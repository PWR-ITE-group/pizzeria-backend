package pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.OrderPromotions;

public interface OrderPromotionsRepository extends JpaRepository<OrderPromotions, Integer> {
}
