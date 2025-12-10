package pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Для "Кухонного экрана" (найти новые или готовящиеся)
    List<Order> findByStatusInOrderByPlacedAtAsc(List<String> statuses);

    // История заказов конкретного сотрудника (официанта)
    List<Order> findByEmployeeId(Long employeeId);
}

