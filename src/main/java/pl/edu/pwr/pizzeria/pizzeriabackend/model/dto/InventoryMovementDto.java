package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementDto {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantityChange;
    private MovementType movementType;  // USE, RESTOCK, ADJUSTMENT
    private LocalDateTime timestamp;
    private Long employeeId;
    private String employeeName;
}

