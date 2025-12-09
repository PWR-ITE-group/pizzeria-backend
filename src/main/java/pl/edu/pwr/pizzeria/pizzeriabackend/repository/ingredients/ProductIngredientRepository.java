package pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredientId;

import java.util.List;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, ProductIngredientId> {
    // Найти рецепт конкретной пиццы
    List<ProductIngredient> findByProductId(Long productId);
}
