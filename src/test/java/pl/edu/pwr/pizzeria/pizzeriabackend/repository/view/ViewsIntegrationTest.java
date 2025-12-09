package pl.edu.pwr.pizzeria.pizzeriabackend.repository.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItem;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.IngredientStockView;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.OrderFullInfo;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.ProductSalesView;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.IngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderItemRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.MenuRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewsIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Для ручного запуска SQL

    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private IngredientStockViewRepository ingredientStockViewRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ProductSalesViewRepository productSalesViewRepository;
    @Autowired
    private OrderFullInfoRepository orderFullInfoRepository;

    @BeforeEach
    void setupViewsInH2() {
        // ХАК ДЛЯ H2:
        // Hibernate создал пустые ТАБЛИЦЫ с именами views. Удаляем их.
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.ingredient_stock_view CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.product_sales_view CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.order_full_info CASCADE");

        // Создаем НАСТОЯЩИЕ VIEWS (копируем SQL из твоего V2 скрипта)

        // 1. Ingredient Stock View
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.ingredient_stock_view AS
            SELECT i.id, i.name, i.unit, i.stock_quantity,
            CASE WHEN i.stock_quantity < 10 THEN 'LOW' ELSE 'OK' END AS stock_status
            FROM pizzeria_schema.ingredients i;
        """);

        // 2. Product Sales View
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.product_sales_view AS
            SELECT p.id AS product_id, p.name,
            COALESCE(SUM(oi.quantity), 0) AS total_sold,
            COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue
            FROM pizzeria_schema.products p
            LEFT JOIN pizzeria_schema.order_items oi ON p.id = oi.product_id
            GROUP BY p.id, p.name;
        """);

        // 3. Order Full Info (Simplified for H2 compatibility if needed, but standard SQL usually works)
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.order_full_info AS
            SELECT o.id AS order_id, o.status AS order_status, o.order_type, o.placed_at, o.updated_at,
            e.name AS employee_name, e.last_name AS employee_last_name,
            d.id AS delivery_id, 
            c.name AS courier_name,
            di.phone AS customer_phone, di.city, di.street
            FROM pizzeria_schema.orders o
            LEFT JOIN pizzeria_schema.employees e ON o.employee_id = e.id
            LEFT JOIN pizzeria_schema.deliveries d ON d.order_id = o.id
            LEFT JOIN pizzeria_schema.employees c ON d.courier_id = c.id
            LEFT JOIN pizzeria_schema.delivery_info di ON di.delivery_id = d.id;
        """);
    }

    @Test
    @DisplayName("VIEW: Ingredient Stock Status (Calculated Field)")
    void shouldCalculateStockStatus() {
        // 1. Создаем ингредиенты
        // Мало (< 10)
        ingredientRepository.save(Ingredient.builder().name("Tomato").unit("kg").stockQuantity(new BigDecimal("5.00")).build());
        // Много (> 10)
        ingredientRepository.save(Ingredient.builder().name("Flour").unit("kg").stockQuantity(new BigDecimal("100.00")).build());

        // 2. Читаем через VIEW
        List<IngredientStockView> viewData = ingredientStockViewRepository.findAll();

        // 3. Проверяем логику CASE WHEN
        assertThat(viewData).hasSize(2);

        IngredientStockView tomato = viewData.stream().filter(i -> i.getName().equals("Tomato")).findFirst().get();
        assertThat(tomato.getStockStatus()).isEqualTo("LOW"); // View работает!

        IngredientStockView flour = viewData.stream().filter(i -> i.getName().equals("Flour")).findFirst().get();
        assertThat(flour.getStockStatus()).isEqualTo("OK");
    }

    @Test
    @DisplayName("VIEW: Product Sales Analytics (Aggregation)")
    void shouldAggregateSales() {
        // Подготовка
        Menu menu = menuRepository.save(new Menu());
        Product pizza = productRepository.save(Product.builder().name("Peperoni").basePrice(BigDecimal.TEN).menu(menu).build());
        Employee emp = employeeRepository.save(Employee.builder().name("Bob").lastName("B").phone("1").login("l").passwordHash("p").role("chef").build());

        // Создаем 2 заказа с этой пиццей
        // Заказ 1: 2 пиццы по 10.00
        createOrderWithItem(emp, pizza, 2, new BigDecimal("10.00"));
        // Заказ 2: 3 пиццы по 10.00
        createOrderWithItem(emp, pizza, 3, new BigDecimal("10.00"));

        // Итого должно быть: Продано 5 штук, Выручка 50.00

        // Читаем через VIEW
        List<ProductSalesView> stats = productSalesViewRepository.findAll();

        assertThat(stats).hasSize(1);
        ProductSalesView stat = stats.get(0);

        assertThat(stat.getTotalSold()).isEqualTo(5L); // 2 + 3
        assertThat(stat.getTotalRevenue()).isEqualByComparingTo("50.00"); // (2*10) + (3*10)
    }

    @Test
    @DisplayName("VIEW: Full Order Info (Complex Join)")
    void shouldJoinAllTablesForOrderInfo() {
        // 1. Создаем сотрудника
        Employee waiter = employeeRepository.save(Employee.builder().name("John").lastName("Wick").phone("9").login("j").passwordHash("p").role("waiter").build());

        // 2. Создаем заказ
        Order order = orderRepository.save(Order.builder().employee(waiter).status("new").orderType("dine_in").placedAt(LocalDateTime.now()).build());

        // 3. Читаем через VIEW
        List<OrderFullInfo> infos = orderFullInfoRepository.findAll();

        assertThat(infos).hasSize(1);
        OrderFullInfo info = infos.get(0);

        // Проверяем, что View подтянул данные из таблицы Employees
        assertThat(info.getOrderId()).isEqualTo(order.getId());
        assertThat(info.getEmployeeName()).isEqualTo("John");
        assertThat(info.getEmployeeLastName()).isEqualTo("Wick");
    }

    // --- Helper ---
    private void createOrderWithItem(Employee emp, Product p, int qty, BigDecimal price) {
        Order o = orderRepository.save(Order.builder().employee(emp).status("done").build());
        orderItemRepository.save(OrderItem.builder().order(o).product(p).quantity(qty).unitPrice(price).status("done").build());
    }
}