package pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.OrderPromotion;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.OrderPromotionId;

import java.util.List;

@Repository
public interface OrderPromotionRepository extends JpaRepository<OrderPromotion, OrderPromotionId> {
    List<OrderPromotion> findByOrder_Id(Long orderId);
    
    boolean existsByOrder_IdAndPromotion_Id(Long orderId, Long promotionId);
    
    void deleteByOrder_IdAndPromotion_Id(Long orderId, Long promotionId);
}

