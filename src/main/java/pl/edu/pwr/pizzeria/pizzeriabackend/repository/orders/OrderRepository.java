package pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusInOrderByPlacedAtAsc(List<String> statuses);

    List<Order> findByEmployeeId(Long employeeId);

    List<Order> findByStatus(OrderStatus status);

    java.util.Optional<Order> findByTrackingToken(String trackingToken);

    java.util.Optional<Order> findByIdAndTrackingTokenIsNotNull(Long id);
}

