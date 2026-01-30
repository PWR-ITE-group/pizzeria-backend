package pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.Delivery;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.DeliveryInfo;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeliveryRepositoryTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryInfoRepository deliveryInfoRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


    @Test
    @DisplayName("Lifecycle: Should create Order -> Assign Courier -> Save Address")
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

        assertThat(savedInfo.getDelivery().getCourier().getLogin()).isEqualTo("speedy_gonzales");
    }

    @Test
    @DisplayName("Courier Tasks: Should find active deliveries for specific courier")
    void shouldFindActiveCourierTasks() {
        Employee courier1 = createCourier("courier_1");
        Employee courier2 = createCourier("courier_2");

        createDelivery(courier1, "assigned");
        createDelivery(courier1, "in_transit");

        createDelivery(courier1, "delivered");

        createDelivery(courier2, "assigned");


        List<Delivery> tasks = deliveryRepository.findByCourierIdAndStatus(courier1.getId(), "assigned");

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getCourier().getLogin()).isEqualTo("courier_1");
    }

    @Test
    @DisplayName("Customer Search: Should find Delivery Info by Phone")
    void shouldFindCustomerByPhone() {
        Delivery delivery = createDelivery(createCourier("c3"), "delivered");

        DeliveryInfo info = DeliveryInfo.builder()
                .delivery(delivery)
                .name("Anna")
                .lastName("Smith")
                .phone("999-999-999")
                .street("Street 1")
                .houseNr("1")
                .city("City")
                .build();
        deliveryInfoRepository.save(info);

        List<DeliveryInfo> results = deliveryInfoRepository.findByPhone("999-999-999");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo("Anna");
    }


    @Test
    @DisplayName("Performance: Process 1,000 Deliveries")
    void testBulkDeliveries() {
        Employee courier = createCourier("bulk_courier");

        int count = 1000;
        List<Order> orders = new ArrayList<>(count);
        List<Delivery> deliveries = new ArrayList<>(count);

        for(int i=0; i<count; i++) {
            orders.add(Order.builder().status(OrderStatus.READY).orderType(OrderType.DELIVERY).build());
        }
        orderRepository.saveAll(orders);
        orderRepository.flush();

        for(int i=0; i<count; i++) {
            deliveries.add(Delivery.builder()
                    .order(orders.get(i))
                    .courier(courier)
                    .status("assigned")
                    .build());
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        deliveryRepository.saveAll(deliveries);
        deliveryRepository.flush();

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Assigned " + count + " deliveries in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(deliveryRepository.count()).isEqualTo(count);
    }


    private Employee createCourier(String login) {
        if(employeeRepository.findByLogin(login).isPresent()) {
            return employeeRepository.findByLogin(login).get();
        }
        return employeeRepository.save(Employee.builder()
                .name("Courier")
                .lastName("Name")
                .phone("Phone_" + login)
                .login(login)
                .passwordHash("pass")
                .role("courier")
                .build());
    }

    private Order createOrder() {
        return orderRepository.save(Order.builder()
                .status(OrderStatus.READY)
                .orderType(OrderType.DELIVERY)
                .placedAt(LocalDateTime.now())
                .build());
    }

    private Delivery createDelivery(Employee courier, String status) {
        return deliveryRepository.save(Delivery.builder()
                .order(createOrder())
                .courier(courier)
                .status(status)
                .assignedAt(LocalDateTime.now())
                .build());
    }
}
