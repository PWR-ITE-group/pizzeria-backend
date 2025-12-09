package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.pwr.pizzeria.pizzeriabackend.AbstractIntegrationTest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ProductIngredient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductIngredientRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void shouldSaveProductWithIngredientsAndQuantity() {
        Menu menu = new Menu();
        menu.setName("Main Menu");
        menu.setIsActive(true);
        menuRepository.save(menu);

        Product pizza = new Product();
        pizza.setName("Pizza Margherita");
        pizza.setBasePrice(new BigDecimal("25.00"));
        pizza.setIsAvailable(true);
        pizza.setMenu(menu);
        productRepository.save(pizza);

        Ingredient cheese = new Ingredient();
        cheese.setName("Mozzarella");
        cheese.setUnit("grams");
        cheese.setStockQuantity(new BigDecimal("1000.00")); // На складе 1 кг
        ingredientRepository.save(cheese);


        ProductIngredient link = new ProductIngredient();
        link.setProduct(pizza);
        link.setIngredient(cheese);
        link.setQuantity(new BigDecimal("150.00")); // 150 грамм сыра на пиццу

        pizza.getProductIngredients().add(link);

        productRepository.save(pizza);

        entityManager.flush();
        entityManager.clear();

        Product foundPizza = productRepository.findById(pizza.getId()).orElseThrow();
        List<ProductIngredient> ingredients = foundPizza.getProductIngredients();

        assertThat(ingredients).hasSize(1);
        assertThat(ingredients.get(0).getIngredient().getName()).isEqualTo("Mozzarella");

        assertThat(ingredients.get(0).getQuantity())
                .isEqualByComparingTo(new BigDecimal("150"));
    }
}
