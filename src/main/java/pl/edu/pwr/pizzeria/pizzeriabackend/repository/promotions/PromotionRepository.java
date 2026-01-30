package pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.Promotion;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCode(String code);
    
    List<Promotion> findByIsActiveTrue();
    
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
}

