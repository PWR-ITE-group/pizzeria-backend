package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.pwr.pizzeria.pizzeriabackend.AbstractIntegrationTest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryRepositoryTest extends AbstractIntegrationTest {

    @Autowired OrderRepository orderRepository;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired DeliveryRepository deliveryRepository;
    @Autowired DeliveryInfoRepository deliveryInfoRepository;

    @Test
    void shouldSaveDeliveryWithCascade(){
        Employee courier = new Employee();
        courier.setName("John");
        courier.setLastName("Doe");
        courier.setPhone("1234567890");
        courier.setLogin("johndoe");
        courier.setPasswordHash("secret");
        courier.setRole("courier");
        courier.setCreatedAt(LocalDateTime.now());

        employeeRepository.save(courier);

        DeliveryInfo info = new DeliveryInfo();
        info.setName("Alice");
        info.setLastName("Smith");
        info.setPhone("987654321");
        info.setStreet("Main Street");
        info.setHouseNr("10");
        info.setCity("Wroclaw");
        info.setPostalCode("50-001");

        Delivery delivery = new Delivery();
        delivery.setOrderStatus(OrderStatus.NEW);
        delivery.setAssignedAt(LocalDateTime.now());

        delivery.setEmployee(courier);

        delivery.setDeliveryInfo(info);

        Order order = new Order();
        order.setOrderStatus(OrderStatus.NEW);
        order.setOrderType(OrderType.DELIVERY);
        order.setPlacedAt(LocalDateTime.now());

        order.setDelivery(delivery);

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getDelivery()).isNotNull();
        assertThat(savedOrder.getDelivery().getId()).isNotNull();

        assertThat(savedOrder.getDelivery().getEmployee()).isNotNull();
        assertThat(savedOrder.getDelivery().getEmployee().getName()).isEqualTo("John");

        assertThat(savedOrder.getDelivery().getDeliveryInfo()).isNotNull();
        assertThat(savedOrder.getDelivery().getDeliveryInfo().getId()).isNotNull();
        assertThat(savedOrder.getDelivery().getDeliveryInfo().getCity()).isEqualTo("Wroclaw");

        assertThat(savedOrder.getDelivery().getOrder().getId()).isEqualTo(savedOrder.getId());
    }
}
