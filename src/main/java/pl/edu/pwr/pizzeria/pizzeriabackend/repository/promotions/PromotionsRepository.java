package pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.Promotions;

import java.util.Optional;

public interface PromotionsRepository extends JpaRepository<Promotions, Integer> {
    Optional<Promotions> findByCode(String code);
}
