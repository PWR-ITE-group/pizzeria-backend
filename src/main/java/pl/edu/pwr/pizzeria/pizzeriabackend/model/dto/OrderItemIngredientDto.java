package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemIngredientDto {
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private String modificationType; // "base", "added", "removed"
}

