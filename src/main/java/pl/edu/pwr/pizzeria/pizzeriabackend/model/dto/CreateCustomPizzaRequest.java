package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomPizzaRequest {
    private String customName;
    private String customDescription;
    private List<IngredientSelectionDto> ingredients;
    private BigDecimal basePrice;
}

