package pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.InventoryMovement;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    // Найти историю движений по конкретному ингредиенту
    List<InventoryMovement> findByIngredientIdOrderByTimestampDesc(Long ingredientId);
}
