package pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.Delivery;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    // Найти активные доставки конкретного курьера
    List<Delivery> findByCourierIdAndStatus(Long courierId, String status);

    // Найти доставку по ID заказа
    Optional<Delivery> findByOrderId(Long orderId);
}

