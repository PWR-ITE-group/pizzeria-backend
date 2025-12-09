package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
}
