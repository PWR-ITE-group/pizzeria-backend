package pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    // Найти ингредиенты, которые заканчиваются (меньше 10 ед)
    // Это аналог твоего View, но на уровне Java
    List<Ingredient> findByStockQuantityLessThan(java.math.BigDecimal amount);
}

