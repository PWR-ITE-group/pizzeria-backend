package pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.Payment;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


    @Test
    @DisplayName("Functional: Should save Payment linked to an Order")
    void shouldSavePaymentForOrder() {
        Order order = createDummyOrder();

        Payment payment = Payment.builder()
                .order(order)
                .amount(new BigDecimal("45.50"))
                .method("card")
                .status("paid")
                .paidAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("Custom Query: Should find Payment by Order ID")
    void shouldFindPaymentByOrderId() {
        Order order = createDummyOrder();
        Payment payment = Payment.builder()
                .order(order)
                .amount(new BigDecimal("100.00"))
                .method("cash")
                .status("pending")
                .build();
        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByOrderId(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("100.00");
    }


    @Test
    @DisplayName("Performance: Process 1,000 Payments")
    void testBulkInsertPayments() {

        List<Order> orders = new ArrayList<>();
        Employee emp = employeeRepository.save(Employee.builder()
                .name("T").lastName("L").phone("000").login("r").passwordHash("x").role("w").build());

        for (int i = 0; i < 1000; i++) {
            orders.add(Order.builder().employee(emp).status(OrderStatus.NEW).orderType(OrderType.DINE_IN).build());
        }
        orderRepository.saveAll(orders);
        orderRepository.flush();

        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            payments.add(Payment.builder()
                    .order(orders.get(i)) // Link 1-to-1
                    .amount(BigDecimal.valueOf(50))
                    .method("card")
                    .status("paid")
                    .build());
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        paymentRepository.saveAll(payments);
        paymentRepository.flush();

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Processed " + 1000 + " payments in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(paymentRepository.count()).isEqualTo(1000);
    }

    private Order createDummyOrder() {
        Employee employee = Employee.builder()
                .name("Waiter")
                .lastName("John")
                .phone("111222333")
                .login("waiter_john")
                .passwordHash("pass")
                .role("waiter")
                .build();
        employeeRepository.save(employee);

        Order order = Order.builder()
                .employee(employee)
                .status(OrderStatus.NEW)
                .orderType(OrderType.DINE_IN)
                .placedAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }
}
