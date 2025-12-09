package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
}
