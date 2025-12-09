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


    @Test
    @DisplayName("Cascade Save: Should save Order AND OrderItems automatically")
    void shouldSaveOrderWithItems() {
        Employee emp = createEmployee();
        Product product = createProduct();

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

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        List<OrderItem> itemsInDb = orderItemRepository.findAll();
        assertThat(itemsInDb).hasSize(1);
        assertThat(itemsInDb.get(0).getProduct().getName()).isEqualTo("Test Pizza");
    }

    @Test
    @DisplayName("Cascade Delete: Should delete OrderItems when Order is deleted")
    void shouldDeleteItemsOnOrderDelete() {
        Employee emp = createEmployee();
        Product product = createProduct();

        Order order = Order.builder().employee(emp).status("new").build();
        OrderItem item = OrderItem.builder().order(order).product(product).quantity(1).unitPrice(BigDecimal.TEN).status("new").build();
        order.setOrderItems(List.of(item));

        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getId();

        assertThat(orderItemRepository.count()).isEqualTo(1);

        orderRepository.deleteById(orderId);
        orderRepository.flush();

        assertThat(orderRepository.findById(orderId)).isEmpty();
        assertThat(orderItemRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kitchen Queue: Should find orders by status and sort by Date ASC")
    void shouldReturnKitchenQueue() {
        Employee emp = createEmployee();

        createOrder(emp, "new", LocalDateTime.now().minusHours(2));

        createOrder(emp, "new", LocalDateTime.now().minusHours(1));

        createOrder(emp, "delivered", LocalDateTime.now().minusHours(3));

        List<Order> queue = orderRepository.findByStatusInOrderByPlacedAtAsc(List.of("new", "preparing"));

        assertThat(queue).hasSize(2);
        assertThat(queue.get(0).getPlacedAt()).isBefore(queue.get(1).getPlacedAt());
    }


    @Test
    @DisplayName("Performance: Insert 1,000 Orders with Items")
    void testBulkInsertOrders() {
        // Подготовка справочников
        Employee emp = createEmployee();
        Product product = createProduct();

        int count = 1000;
        List<Order> orders = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Order order = Order.builder()
                    .employee(emp)
                    .status("new")
                    .orderType("pickup")
                    .placedAt(LocalDateTime.now())
                    .build();

            OrderItem item1 = OrderItem.builder().order(order).product(product).quantity(1).unitPrice(BigDecimal.TEN).status("new").build();
            OrderItem item2 = OrderItem.builder().order(order).product(product).quantity(2).unitPrice(BigDecimal.TEN).status("new").build();

            order.setOrderItems(List.of(item1, item2));
            orders.add(order);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        orderRepository.saveAll(orders);
        orderRepository.flush();

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Inserted " + count + " orders (approx 2000 items) in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("Average time per order chain: " + ((double) stopWatch.getTotalTimeMillis() / count) + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(orderRepository.count()).isEqualTo(count);
        assertThat(orderItemRepository.count()).isEqualTo(count * 2);
    }


    private Employee createEmployee() {
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

    private void createOrder(Employee emp, String status, LocalDateTime placedAt) {
        orderRepository.save(Order.builder()
                .employee(emp)
                .status(status)
                .placedAt(placedAt)
                .orderType("dine_in")
                .build());
    }
}
