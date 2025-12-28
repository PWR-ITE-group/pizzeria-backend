package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.IngredientUnit;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientDto {
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private IngredientUnit unit;
}

