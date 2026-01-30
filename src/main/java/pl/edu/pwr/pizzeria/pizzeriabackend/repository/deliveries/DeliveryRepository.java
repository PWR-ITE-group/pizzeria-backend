package pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.Delivery;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByCourierIdAndStatus(Long courierId, String status);

    Optional<Delivery> findByOrderId(Long orderId);
}

