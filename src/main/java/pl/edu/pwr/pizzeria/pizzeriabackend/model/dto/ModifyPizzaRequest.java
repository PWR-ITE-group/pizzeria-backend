package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyPizzaRequest {
    private Long productId;
    private List<IngredientSelectionDto> addedIngredients;
    private List<Long> removedIngredientIds;
}

