package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientId implements java.io.Serializable {
    private Long productId;
    private Long ingredientId;
}
