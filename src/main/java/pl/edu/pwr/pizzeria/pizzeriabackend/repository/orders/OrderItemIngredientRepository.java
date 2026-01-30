package pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItemIngredient;

import java.util.List;

@Repository
public interface OrderItemIngredientRepository extends JpaRepository<OrderItemIngredient, Long> {
    List<OrderItemIngredient> findByOrderItem_Id(Long orderItemId);
    
    void deleteByOrderItem_Id(Long orderItemId);
}

