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

    // --- SECTION 1: FUNCTIONAL TESTS ---

    @Test
    @DisplayName("Lifecycle: Should create Order -> Assign Courier -> Save Address")
    void shouldCreateFullDeliveryChain() {
        // 1. GIVEN: Prepare Courier and Order
        Employee courier = createCourier("speedy_gonzales");
        Order order = createOrder();

        // 2. WHEN: Create Delivery
        Delivery delivery = Delivery.builder()
                .order(order)
                .courier(courier)
                .status("assigned")
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // 3. WHEN: Create Delivery Info (Address) linked to Delivery
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

        // 4. THEN: Verify links
        assertThat(savedDelivery.getId()).isNotNull();
        assertThat(savedInfo.getId()).isNotNull();

        // Check navigation: Info -> Delivery -> Courier
        assertThat(savedInfo.getDelivery().getCourier().getLogin()).isEqualTo("speedy_gonzales");
    }

    @Test
    @DisplayName("Courier Tasks: Should find active deliveries for specific courier")
    void shouldFindActiveCourierTasks() {
        Employee courier1 = createCourier("courier_1");
        Employee courier2 = createCourier("courier_2");

        // Courier 1 has 2 active tasks
        createDelivery(courier1, "assigned");
        createDelivery(courier1, "in_transit");

        // Courier 1 has 1 finished task (should not be found)
        createDelivery(courier1, "delivered");

        // Courier 2 has 1 active task (should not be found for Courier 1)
        createDelivery(courier2, "assigned");

        // WHEN: Search for active tasks for Courier 1
        // We simulate the query: WHERE courier_id = ? AND status IN ('assigned', 'in_transit')
        // But since your repository method finds by ONE status, let's test that first.
        List<Delivery> tasks = deliveryRepository.findByCourierIdAndStatus(courier1.getId(), "assigned");

        // THEN
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getCourier().getLogin()).isEqualTo("courier_1");
    }

    @Test
    @DisplayName("Customer Search: Should find Delivery Info by Phone")
    void shouldFindCustomerByPhone() {
        // 1. Save info
        Delivery delivery = createDelivery(createCourier("c3"), "delivered");

        DeliveryInfo info = DeliveryInfo.builder()
                .delivery(delivery)
                .name("Anna")
                .lastName("Smith")
                .phone("999-999-999") // Target phone
                .street("Street 1")
                .houseNr("1")
                .city("City")
                .build();
        deliveryInfoRepository.save(info);

        // 2. Search
        List<DeliveryInfo> results = deliveryInfoRepository.findByPhone("999-999-999");

        // 3. Verify
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo("Anna");
    }

    // --- SECTION 2: PERFORMANCE TESTS ---

    @Test
    @DisplayName("Performance: Process 1,000 Deliveries")
    void testBulkDeliveries() {
        // Setup data
        Employee courier = createCourier("bulk_courier");

        int count = 1000;
        List<Order> orders = new ArrayList<>(count);
        List<Delivery> deliveries = new ArrayList<>(count);

        // Pre-generate Orders
        for(int i=0; i<count; i++) {
            orders.add(Order.builder().status("ready").orderType("delivery").build());
        }
        orderRepository.saveAll(orders);
        orderRepository.flush();

        // Generate Deliveries
        for(int i=0; i<count; i++) {
            deliveries.add(Delivery.builder()
                    .order(orders.get(i))
                    .courier(courier)
                    .status("assigned")
                    .build());
        }

        // MEASURE
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

    // --- HELPER METHODS ---

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
                .status("ready")
                .orderType("delivery")
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
