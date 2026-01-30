package pl.edu.pwr.pizzeria.pizzeriabackend.repository.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.Delivery;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.DeliveryInfo;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.InventoryMovement;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItem;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.Promotions;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries.DeliveryInfoRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries.DeliveryRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.IngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.InventoryMovementRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderItemRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.MenuRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions.PromotionsRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewsIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    @Autowired
    private OrderItemsInfoViewRepository orderItemsInfoViewRepository;
    @Autowired
    private ActivePromotionsViewRepository activePromotionsViewRepository;
    @Autowired
    private PromotionsRepository promotionsRepository;
    @Autowired
    private InventoryHistoryViewRepository inventoryHistoryViewRepository;
    @Autowired
    private DeliveryStatusViewRepository deliveryStatusViewRepository;
    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private DeliveryInfoRepository deliveryInfoRepository;

    @BeforeEach
    void setupViewsInH2() {

        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.ingredient_stock_view CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.product_sales_view CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.order_full_info CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.order_items_view CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.active_promotions CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.inventory_history_view CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pizzeria_schema.delivery_status_view CASCADE");

        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.ingredient_stock_view AS
            SELECT i.id, i.name, i.unit, i.stock_quantity,
            CASE WHEN i.stock_quantity < 10 THEN 'LOW' ELSE 'OK' END AS stock_status
            FROM pizzeria_schema.ingredients i;
        """);

        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.product_sales_view AS
            SELECT p.id AS product_id, p.name,
            COALESCE(SUM(oi.quantity), 0) AS total_sold,
            COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue
            FROM pizzeria_schema.products p
            LEFT JOIN pizzeria_schema.order_items oi ON p.id = oi.product_id
            GROUP BY p.id, p.name;
        """);

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

        // 4. Order Items View
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.order_items_view AS
            SELECT
                oi.order_id,
                oi.product_id,
                p.name AS product_name,
                oi.quantity,
                oi.unit_price,
                (oi.quantity * oi.unit_price) AS total_price,
                oi.status
            FROM pizzeria_schema.order_items oi
                     JOIN pizzeria_schema.products p ON p.id = oi.product_id;
        """);

        // 5. Active Promotions View
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.active_promotions AS
            SELECT *
            FROM pizzeria_schema.promotions
            WHERE is_active = TRUE
              AND (valid_from IS NULL OR valid_from <= CURRENT_DATE)
              AND (valid_to   IS NULL OR valid_to   >= CURRENT_DATE);
        """);

        // 6. Inventory History View
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.inventory_history_view AS
            SELECT
                m.id,
                m.ingredient_id,
                i.name AS ingredient_name,
                m.quantity_change,
                m.movement_type,
                m.timestamp,
                e.name AS employee_name,
                e.last_name AS employee_last_name
            FROM pizzeria_schema.inventory_movements m
                     JOIN pizzeria_schema.ingredients i ON i.id = m.ingredient_id
                     LEFT JOIN pizzeria_schema.employees e ON e.id = m.employee_id;
        """);

        // 7. Delivery Status View
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW pizzeria_schema.delivery_status_view AS
            SELECT
                d.id AS delivery_id,
                d.status,
                d.assigned_at,
                d.delivered_at,
                o.id AS order_id,
                o.status AS order_status,
                o.placed_at,
                c.name AS courier_name,
                c.last_name AS courier_last_name,
                di.city,
                di.street,
                di.house_nr
            FROM pizzeria_schema.deliveries d
                     JOIN pizzeria_schema.orders o ON o.id = d.order_id
                     LEFT JOIN pizzeria_schema.employees c ON c.id = d.courier_id
                     LEFT JOIN pizzeria_schema.delivery_info di ON di.delivery_id = d.id;
        """);
    }

    @Test
    @DisplayName("VIEW: Ingredient Stock Status (Calculated Field)")
    void shouldCalculateStockStatus() {
        ingredientRepository.save(Ingredient.builder().name("Tomato").unit(IngredientUnit.KG).stockQuantity(new BigDecimal("5.00")).build());
        ingredientRepository.save(Ingredient.builder().name("Flour").unit(IngredientUnit.KG).stockQuantity(new BigDecimal("100.00")).build());

        List<IngredientStockView> viewData = ingredientStockViewRepository.findAll();

        assertThat(viewData).hasSize(2);

        IngredientStockView tomato = viewData.stream().filter(i -> i.getName().equals("Tomato")).findFirst().get();
        assertThat(tomato.getStockStatus()).isEqualTo("LOW"); // View работает!

        IngredientStockView flour = viewData.stream().filter(i -> i.getName().equals("Flour")).findFirst().get();
        assertThat(flour.getStockStatus()).isEqualTo("OK");
    }

    @Test
    @DisplayName("VIEW: Product Sales Analytics (Aggregation)")
    void shouldAggregateSales() {
        Menu menu = menuRepository.save(new Menu());
        Product pizza = productRepository.save(Product.builder().name("Peperoni").basePrice(BigDecimal.TEN).menu(menu).build());
        Employee emp = employeeRepository.save(Employee.builder().name("Bob").lastName("B").phone("1").login("l").passwordHash("p").role("chef").build());

        createOrderWithItem(emp, pizza, 2, new BigDecimal("10.00"));
        createOrderWithItem(emp, pizza, 3, new BigDecimal("10.00"));


        List<ProductSalesView> stats = productSalesViewRepository.findAll();

        assertThat(stats).hasSize(1);
        ProductSalesView stat = stats.get(0);

        assertThat(stat.getTotalSold()).isEqualTo(5L); // 2 + 3
        assertThat(stat.getTotalRevenue()).isEqualByComparingTo("50.00"); // (2*10) + (3*10)
    }

    @Test
    @DisplayName("VIEW: Product Sales - Should handle ZERO sales correctly")
    void shouldReturnZeroForUnsoldProduct() {
        Menu menu = menuRepository.save(new Menu());
        Product unpopularPizza = new Product();
        unpopularPizza.setName("Pineapple Pizza");
        unpopularPizza.setBasePrice(new BigDecimal("20.00"));
        unpopularPizza.setMenu(menu);
        unpopularPizza.setIsAvailable(true);
        productRepository.save(unpopularPizza);

        Product popularPizza = productRepository.save(Product.builder().name("Salami").basePrice(BigDecimal.TEN).menu(menu).build());
        createOrderWithItem(null, popularPizza, 1, BigDecimal.TEN);

        List<ProductSalesView> stats = productSalesViewRepository.findAll();

        ProductSalesView pineappleStat = stats.stream()
                .filter(s -> s.getName().equals("Pineapple Pizza"))
                .findFirst()
                .orElseThrow();

        assertThat(pineappleStat.getTotalSold()).isEqualTo(0L);
        assertThat(pineappleStat.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("VIEW: Full Order Info (Complex Join)")
    void shouldJoinAllTablesForOrderInfo() {
        Employee waiter = employeeRepository.save(Employee.builder().name("John").lastName("Wick").phone("9").login("j").passwordHash("p").role("waiter").build());

        Order order = orderRepository.save(Order.builder().employee(waiter).status(OrderStatus.NEW).orderType(OrderType.DINE_IN).placedAt(LocalDateTime.now()).build());

        List<OrderFullInfo> infos = orderFullInfoRepository.findAll();

        assertThat(infos).hasSize(1);
        OrderFullInfo info = infos.get(0);

        assertThat(info.getOrderId()).isEqualTo(order.getId());
        assertThat(info.getEmployeeName()).isEqualTo("John");
        assertThat(info.getEmployeeLastName()).isEqualTo("Wick");
    }

    @Test
    @DisplayName("VIEW: Order Items View - Join and Total Price Calculation")
    void shouldJoinTablesForOrderItems() {
        Employee emp = employeeRepository.save(Employee.builder().name("Alice").lastName("A").phone("8").login("a").passwordHash("p").role("chef").build());

        Product product = productRepository.save(Product.builder().name("Margarita").basePrice(new BigDecimal("25.00")).menu(menuRepository.save(new Menu())).build());

        Order order = Order.builder()
                .employee(emp)
                .status("new")
                .orderType("dine_in")
                .placedAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .build();

        OrderItem item1 = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .status("pending")
                .build();

        order.setOrderItems(List.of(item1));
        orderRepository.save(order);

        List<OrderItemsInfoView> infos = orderItemsInfoViewRepository.findAll();

        assertThat(infos).hasSize(1);

        OrderItemsInfoView info = infos.get(0);

        assertThat(info.getOrderId()).isEqualTo(order.getId());
        assertThat(info.getProductName()).isEqualTo("Margarita");
        assertThat(info.getProductId()).isEqualTo(product.getId());
        assertThat(info.getQuantity()).isEqualTo(2);
        assertThat(info.getUnitPrice()).isEqualByComparingTo("25.00");
        assertThat(info.getTotalPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("VIEW: Order Items View - Search by ID")
    void shouldFindById() {
        Employee emp = employeeRepository.save(Employee.builder().name("Alice").lastName("A").phone("8").login("a").passwordHash("p").role("chef").build());

        Product product = productRepository.save(Product.builder().name("Margarita").basePrice(new BigDecimal("25.00")).menu(menuRepository.save(new Menu())).build());
        Product product1 = productRepository.save(Product.builder().name("Pepperoni").basePrice(new BigDecimal("30.00")).menu(menuRepository.save(new Menu())).build());

        productRepository.save(product);
        productRepository.save(product1);

        Order order = Order.builder()
                .employee(emp)
                .status("new")
                .orderType("dine_in")
                .placedAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .build();

        OrderItem item1 = OrderItem.builder()
                .order(order)
                .product(product1)
                .quantity(2)
                .unitPrice(product1.getBasePrice())
                .status("pending")
                .build();

        OrderItem item2 = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(4)
                .unitPrice(product.getBasePrice())
                .status("pending")
                .build();

        order.setOrderItems(List.of(item1, item2));
        orderRepository.save(order);

        List<OrderItemsInfoView> infos = orderItemsInfoViewRepository.findAll();

        assertThat(infos).hasSize(2);

        assertThat(infos)
                .extracting(OrderItemsInfoView::getProductName, OrderItemsInfoView::getTotalPrice)
                .containsExactlyInAnyOrder(
                        tuple("Pepperoni", new BigDecimal("60.00")),
                        tuple("Margarita", new BigDecimal("100.00"))
                );
    }

    @Test
    @DisplayName("VIEW: Active Promotions - Should return only currently active promotions")
    void shouldReturnOnlyActivePromotions() {
        LocalDate today = LocalDate.now();


        Promotions activePromo = Promotions.builder()
                .code("ACTIVE_NOW")
                .discount_percent(BigDecimal.TEN)
                .isActive(true)
                .validFrom(today.minusDays(5))
                .validTo(today.plusDays(5))
                .build();

        Promotions foreverPromo = Promotions.builder()
                .code("FOREVER")
                .discount_percent(BigDecimal.valueOf(5))
                .isActive(true)
                .validFrom(null)
                .validTo(null)
                .build();

        Promotions disabledPromo = Promotions.builder()
                .code("DISABLED")
                .discount_percent(BigDecimal.TEN)
                .isActive(false) // <--- Выключена вручную
                .validFrom(today.minusDays(5))
                .validTo(today.plusDays(5))
                .build();

        Promotions futurePromo = Promotions.builder()
                .code("FUTURE")
                .discount_percent(BigDecimal.TEN)
                .isActive(true)
                .validFrom(today.plusDays(1))
                .validTo(today.plusDays(10))
                .build();

        Promotions expiredPromo = Promotions.builder()
                .code("EXPIRED")
                .discount_percent(BigDecimal.TEN)
                .isActive(true)
                .validFrom(today.minusDays(10))
                .validTo(today.minusDays(1))
                .build();

        promotionsRepository.saveAll(List.of(activePromo, foreverPromo, disabledPromo, futurePromo, expiredPromo));

        List<ActivePromotionsView> result = activePromotionsViewRepository.findAll();


        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(ActivePromotionsView::getCode)
                .containsExactlyInAnyOrder("ACTIVE_NOW", "FOREVER");

        ActivePromotionsView viewItem = result.stream()
                .filter(p -> p.getCode().equals("ACTIVE_NOW"))
                .findFirst()
                .orElseThrow();

        assertThat(viewItem.getDiscount_percent()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("VIEW: Inventory History - Should join Ingredient and Employee names")
    void shouldReturnInventoryHistory() {
        Ingredient cheese = ingredientRepository.save(Ingredient.builder()
                .name("Mozzarella")
                .unit("kg")
                .stockQuantity(BigDecimal.ZERO)
                .build());

        Employee chef = employeeRepository.save(Employee.builder()
                .name("Luigi")
                .lastName("Mario")
                .phone("555")
                .login("luigi")
                .passwordHash("x")
                .role("chef")
                .build());

        InventoryMovement movement = new InventoryMovement();
        movement.setIngredient(cheese);
        movement.setEmployee(chef);
        movement.setMovementType("restock");
        movement.setQuantityChange(new BigDecimal("10.00"));
        movement.setTimestamp(LocalDateTime.now());

        inventoryMovementRepository.save(movement);

        List<InventoryHistoryView> history = inventoryHistoryViewRepository.findAll();

        assertThat(history).hasSize(1);

        var record = history.get(0);

        assertThat(record.getIngredientId()).isEqualTo(cheese.getId());

        assertThat(record.getIngredientName()).isEqualTo("Mozzarella");
        assertThat(record.getEmployeeName()).isEqualTo("Luigi");
        assertThat(record.getEmployeeLastName()).isEqualTo("Mario");
        assertThat(record.getQuantityChange()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Lifecycle: Should create Order -> Assign Courier -> Save Delivery -> Save Address")
    void shouldCreateFullDeliveryChain() {
        Employee courier = createCourier("speedy_gonzales");
        Order order = createOrder();

        Delivery delivery = Delivery.builder()
                .order(order)
                .courier(courier)
                .status("assigned")
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryInfo info = DeliveryInfo.builder()
                .delivery(savedDelivery)
                .name("Jan")
                .lastName("Kowalski")
                .phone("500600700")
                .street("Main St")
                .houseNr("10")
                .city("Wroclaw")
                .postalCode("50-000")
                .build();

        DeliveryInfo savedInfo = deliveryInfoRepository.save(info);

        assertThat(savedDelivery.getId()).isNotNull();
        assertThat(savedInfo.getId()).isNotNull();

        assertThat(savedInfo.getDelivery().getId()).isEqualTo(savedDelivery.getId());
        assertThat(savedInfo.getDelivery().getCourier().getLogin()).isEqualTo("speedy_gonzales");
    }

    // --- Helper ---
    private void createOrderWithItem(Employee emp, Product p, int qty, BigDecimal price) {
        Order o = orderRepository.save(Order.builder().employee(emp).status(OrderStatus.COMPLETED).build());
        orderItemRepository.save(OrderItem.builder().order(o).product(p).quantity(qty).unitPrice(price).status(OrderItemStatus.READY).build());
    }

    private Employee createCourier(String login) {
        if(employeeRepository.findByLogin(login).isPresent()) {
            return employeeRepository.findByLogin(login).get();
        }
        return employeeRepository.save(Employee.builder()
                .name("Speedy")
                .lastName("Name")
                .phone("Phone_" + login)
                .login(login)
                .passwordHash("pass")
                .role("courier")
                .build());
    }

    private Order createOrder() {
        return orderRepository.save(Order.builder()
                .status("in_delivery")
                .orderType("delivery")
                .placedAt(LocalDateTime.now())
                .build());
    }
}