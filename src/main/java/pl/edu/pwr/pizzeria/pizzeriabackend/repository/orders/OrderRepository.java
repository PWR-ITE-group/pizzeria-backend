package pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Для "Кухонного экрана" (найти новые или готовящиеся)
    List<Order> findByStatusInOrderByPlacedAtAsc(List<String> statuses);

    // История заказов конкретного сотрудника (официанта)
    List<Order> findByEmployeeId(Long employeeId);

    // Find orders by status (using enum)
    List<Order> findByStatus(OrderStatus status);

    // Find order by tracking token (for public access)
    java.util.Optional<Order> findByTrackingToken(String trackingToken);

    // Find order by ID if it has a tracking token (for public access by ID)
    java.util.Optional<Order> findByIdAndTrackingTokenIsNotNull(Long id);
}

