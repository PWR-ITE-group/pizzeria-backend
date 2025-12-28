package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestockRequest {
    private Long ingredientId;
    private BigDecimal quantity;  // Must be positive
    // employeeId is extracted automatically from JWT token
}

