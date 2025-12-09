package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ProductIngredient;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Integer> {
}
