package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.pwr.pizzeria.pizzeriabackend.AbstractIntegrationTest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.OrderItem;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MenuRepository menuRepository;

    @Test
    void shouldSaveOrderWithItemsCascade(){
        Menu menu = new Menu();
        menu.setName("Test Menu");
        menu.setIsActive(true);
        menuRepository.save(menu);

        Product product = new Product();
        product.setName("Pizza Pepperoni");
        product.setBasePrice(new BigDecimal("30.50"));
        product.setIsAvailable(true);
        product.setMenu(menu);
        productRepository.save(product);

        Order order = new Order();
        order.setOrderStatus(OrderStatus.NEW);
        order.setOrderType(OrderType.DELIVERY);
        order.setPlacedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setProduct(product); // Ссылка на продукт
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("30.50"));
        item.setStatus("pending");

        order.addOrderItem(item);
        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        assertThat(savedOrder.getOrderItems().get(0).getId()).isNotNull();

        assertThat(savedOrder.getOrderItems().get(0).getProduct().getName()).isEqualTo("Pizza Pepperoni");
    }
}
