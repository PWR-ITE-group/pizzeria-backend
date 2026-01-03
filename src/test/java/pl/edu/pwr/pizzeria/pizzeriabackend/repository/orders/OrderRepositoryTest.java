package pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItem;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
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
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MenuRepository menuRepository;

    // --- БЛОК 1: ФУНКЦИОНАЛЬНЫЕ ТЕСТЫ (СВЯЗИ) ---

    @Test
    @DisplayName("Cascade Save: Should save Order AND OrderItems automatically")
    void shouldSaveOrderWithItems() {
        // 1. ПОДГОТОВКА ДАННЫХ (Employee, Menu, Product)
        Employee emp = createEmployee();
        Product product = createProduct();

        // 2. СОЗДАЕМ ЗАКАЗ
        Order order = Order.builder()
                .employee(emp)
                .status(OrderStatus.NEW)
                .orderType(OrderType.DINE_IN)
                .placedAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO) // В H2 триггер не сработает, ставим 0
                .build();

        // 3. ДОБАВЛЯЕМ ПОЗИЦИИ (Items)
        // Важно: создаем связь в обе стороны для корректной работы Hibernate
        OrderItem item1 = OrderItem.builder()
                .order(order)       // Ссылка на родителя
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .status(OrderItemStatus.PENDING)
                .build();

        // Если в Entity Order есть поле List<OrderItem>, добавляем туда
        order.setOrderItems(List.of(item1));

        // 4. WHEN: Сохраняем ТОЛЬКО Заказ
        Order savedOrder = orderRepository.save(order);

        // 5. THEN: Проверяем, что Hibernate сам сохранил и OrderItem
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        // Проверяем через репозиторий позиций
        List<OrderItem> itemsInDb = orderItemRepository.findAll();
        assertThat(itemsInDb).hasSize(1);
        assertThat(itemsInDb.get(0).getProduct().getName()).isEqualTo("Test Pizza");
    }

    @Test
    @DisplayName("Cascade Delete: Should delete OrderItems when Order is deleted")
    void shouldDeleteItemsOnOrderDelete() {
        // 1. Создаем и сохраняем заказ с позицией
        Employee emp = createEmployee();
        Product product = createProduct();

        Order order = Order.builder().employee(emp).status(OrderStatus.NEW).build();
        OrderItem item = OrderItem.builder().order(order).product(product).quantity(1).unitPrice(BigDecimal.TEN).status(OrderItemStatus.PENDING).build();
        order.setOrderItems(List.of(item));

        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getId();

        // Убеждаемся, что всё сохранилось
        assertThat(orderItemRepository.count()).isEqualTo(1);

        // 2. WHEN: Удаляем заказ
        orderRepository.deleteById(orderId);
        orderRepository.flush(); // Принудительно

        // 3. THEN: Позиции тоже должны исчезнуть
        assertThat(orderRepository.findById(orderId)).isEmpty();
        assertThat(orderItemRepository.count()).isEqualTo(0); // Магия CASCADE
    }

    @Test
    @DisplayName("Kitchen Queue: Should find orders by status and sort by Date ASC")
    void shouldReturnKitchenQueue() {
        Employee emp = createEmployee();

        // Создаем 3 заказа с разным временем и статусами
        // Заказ 1 (Старый, New) -> Должен быть первым
        createOrder(emp, OrderStatus.NEW, LocalDateTime.now().minusHours(2));

        // Заказ 2 (Новый, New) -> Должен быть вторым
        createOrder(emp, OrderStatus.NEW, LocalDateTime.now().minusHours(1));

        // Заказ 3 (Старый, но Delivered) -> Не должен попасть в выборку
        createOrder(emp, OrderStatus.DELIVERED, LocalDateTime.now().minusHours(3));

        // WHEN: Ищем заказы для кухни (new или preparing)
        List<Order> queue = orderRepository.findByStatusInOrderByPlacedAtAsc(List.of(OrderStatus.NEW.getDbValue(), OrderStatus.PREPARING.getDbValue()));

        // THEN
        assertThat(queue).hasSize(2);
        // Проверяем порядок (FIFO - первый пришел, первый ушел)
        assertThat(queue.get(0).getPlacedAt()).isBefore(queue.get(1).getPlacedAt());
    }

    // --- БЛОК 2: ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ ---

    @Test
    @DisplayName("Performance: Insert 1,000 Orders with Items")
    void testBulkInsertOrders() {
        // Подготовка справочников
        Employee emp = createEmployee();
        Product product = createProduct();

        int count = 1000;
        List<Order> orders = new ArrayList<>(count);

        // Генерируем 1000 заказов в памяти
        for (int i = 0; i < count; i++) {
            Order order = Order.builder()
                    .employee(emp)
                    .status(OrderStatus.NEW)
                    .orderType(OrderType.PICKUP)
                    .placedAt(LocalDateTime.now())
                    .build();

            // К каждому заказу добавляем по 2 пиццы
            OrderItem item1 = OrderItem.builder().order(order).product(product).quantity(1).unitPrice(BigDecimal.TEN).status(OrderItemStatus.PENDING).build();
            OrderItem item2 = OrderItem.builder().order(order).product(product).quantity(2).unitPrice(BigDecimal.TEN).status(OrderItemStatus.PENDING).build();

            order.setOrderItems(List.of(item1, item2));
            orders.add(order);
        }

        // ЗАМЕР ВРЕМЕНИ
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        orderRepository.saveAll(orders);
        orderRepository.flush();

        stopWatch.stop();

        // ВЫВОД
        System.out.println("--------------------------------------------------");
        System.out.println("Inserted " + count + " orders (approx 2000 items) in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("Average time per order chain: " + ((double) stopWatch.getTotalTimeMillis() / count) + " ms");
        System.out.println("--------------------------------------------------");

        // Проверка
        assertThat(orderRepository.count()).isEqualTo(count);
        assertThat(orderItemRepository.count()).isEqualTo(count * 2);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (чтобы не дублировать код) ---

    private Employee createEmployee() {
        // Проверяем, есть ли уже, чтобы не падать на Unique Constraint в тестах
        if(employeeRepository.count() > 0) return employeeRepository.findAll().get(0);

        return employeeRepository.save(Employee.builder()
                .name("Chef")
                .lastName("Mario")
                .phone("999888777")
                .login("chef_mario")
                .passwordHash("pass")
                .role("chef")
                .build());
    }

    private Product createProduct() {
        if(productRepository.count() > 0) return productRepository.findAll().get(0);

        Menu menu = menuRepository.save(new Menu());
        return productRepository.save(Product.builder()
                .name("Test Pizza")
                .basePrice(new BigDecimal("20.00"))
                .menu(menu)
                .build());
    }

    private void createOrder(Employee emp, OrderStatus status, LocalDateTime placedAt) {
        orderRepository.save(Order.builder()
                .employee(emp)
                .status(status)
                .placedAt(placedAt)
                .orderType(OrderType.DINE_IN)
                .build());
    }
}
