package pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.InventoryMovement;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.MenuRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ProductIngredientRepository productIngredientRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // --- БЛОК 1: ТЕСТЫ РЕЦЕПТУРЫ (Many-to-Many с доп. полем) ---

    @Test
    @DisplayName("Recipe: Should save ProductIngredient (Composite Key)")
    void shouldSaveRecipe() {
        // 1. Создаем Ингредиент (Мука)
        Ingredient flour = ingredientRepository.save(Ingredient.builder()
                .name("Flour")
                .unit("g")
                .stockQuantity(BigDecimal.valueOf(10000))
                .build());

        // 2. Создаем Продукт (Пицца)
        Menu menu = menuRepository.save(new Menu());
        Product pizza = productRepository.save(Product.builder()
                .name("Margherita")
                .basePrice(BigDecimal.TEN)
                .menu(menu)
                .build());

        // 3. Создаем связь
        ProductIngredient recipeItem = new ProductIngredient();

        // ВАЖНО: Заполняем ID (для базы данных)
        recipeItem.setProductId(pizza.getId());
        recipeItem.setIngredientId(flour.getId());

        // ВАЖНО: Заполняем ССЫЛКИ (для Hibernate и Java-объекта в тесте)
        // Без этого getIngredient() вернет null, так как объект берется из кэша
        recipeItem.setProduct(pizza);
        recipeItem.setIngredient(flour);

        recipeItem.setQuantity(new BigDecimal("300.00"));

        productIngredientRepository.save(recipeItem);

        // Чтобы быть на 100% уверенным, что данные идут из БД, а не из кэша,
        // можно (но не обязательно, если заполнили поля выше) сделать так:
        // productIngredientRepository.flush();
        // entityManager.clear(); // Требует инжекта EntityManager

        // 4. Проверяем
        List<ProductIngredient> ingredients = productIngredientRepository.findByProductId(pizza.getId());

        assertThat(ingredients).hasSize(1);
        // Теперь здесь не будет NULL, потому что мы сделали setIngredient(flour)
        assertThat(ingredients.get(0).getIngredient().getName()).isEqualTo("Flour");
        assertThat(ingredients.get(0).getQuantity()).isEqualByComparingTo("300.00");
    }

    // --- БЛОК 2: ТЕСТЫ СКЛАДА (Inventory) ---

    @Test
    @DisplayName("Low Stock: Should find ingredients running low")
    void shouldFindLowStockIngredients() {
        // 1. Создаем ингредиенты с разным кол-вом
        createIngredient("Tomatoes", 5.0);   // Мало
        createIngredient("Cheese", 100.0);   // Много
        createIngredient("Basil", 2.0);      // Мало

        // 2. Ищем те, у кого меньше 10 единиц
        List<Ingredient> lowStock = ingredientRepository.findByStockQuantityLessThan(new BigDecimal("10.00"));

        // 3. Проверяем
        assertThat(lowStock).hasSize(2);
        assertThat(lowStock).extracting(Ingredient::getName)
                .containsExactlyInAnyOrder("Tomatoes", "Basil");
        assertThat(lowStock).extracting(Ingredient::getName)
                .doesNotContain("Cheese");
    }

    @Test
    @DisplayName("Movements: Should save history and sort by Date")
    void shouldTrackInventoryMovements() {
        Ingredient cheese = createIngredient("Mozzarella", 50.0);
        Employee manager = createEmployee();

        // 1. Добавляем движения
        // Старое движение (Restock)
        InventoryMovement move1 = InventoryMovement.builder()
                .ingredient(cheese)
                .employee(manager)
                .movementType("restock")
                .quantityChange(BigDecimal.valueOf(10))
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();

        // Новое движение (Use)
        InventoryMovement move2 = InventoryMovement.builder()
                .ingredient(cheese)
                .employee(manager)
                .movementType("use")
                .quantityChange(BigDecimal.valueOf(-5))
                .timestamp(LocalDateTime.now())
                .build();

        inventoryMovementRepository.saveAll(List.of(move1, move2));

        // 2. Достаем историю
        List<InventoryMovement> history = inventoryMovementRepository.findByIngredientIdOrderByTimestampDesc(cheese.getId());

        // 3. Проверяем (Свежее сверху)
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getMovementType()).isEqualTo("use");     // Newest
        assertThat(history.get(1).getMovementType()).isEqualTo("restock"); // Oldest
    }

    // --- БЛОК 3: ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ ---

    @Test
    @DisplayName("Performance: Insert 10,000 Inventory Movements")
    void testBulkInventoryMovements() {
        Ingredient ing = createIngredient("TestItem", 1000.0);
        Employee emp = createEmployee();

        int count = 10000;
        List<InventoryMovement> batch = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            batch.add(InventoryMovement.builder()
                    .ingredient(ing)
                    .employee(emp)
                    .quantityChange(BigDecimal.ONE)
                    .movementType("restock")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        inventoryMovementRepository.saveAll(batch);
        inventoryMovementRepository.flush();

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Logged " + count + " warehouse movements in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(inventoryMovementRepository.count()).isEqualTo(count);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    private Ingredient createIngredient(String name, double qty) {
        return ingredientRepository.save(Ingredient.builder()
                .name(name)
                .unit("kg")
                .stockQuantity(BigDecimal.valueOf(qty))
                .build());
    }

    private Employee createEmployee() {
        if (employeeRepository.count() > 0) return employeeRepository.findAll().get(0);
        return employeeRepository.save(Employee.builder()
                .name("Manager")
                .lastName("Doe")
                .phone("123")
                .login("admin")
                .passwordHash("x")
                .role("manager")
                .build());
    }
}
