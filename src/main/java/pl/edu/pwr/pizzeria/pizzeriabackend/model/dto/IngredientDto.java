package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.IngredientStockStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.IngredientUnit;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {
    private Long id;
    private String name;
    private IngredientUnit unit;
    private BigDecimal stockQuantity;
    private IngredientStockStatus stockStatus;
}