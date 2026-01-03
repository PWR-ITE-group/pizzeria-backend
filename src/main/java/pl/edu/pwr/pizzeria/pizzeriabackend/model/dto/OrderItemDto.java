package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;  // quantity * unitPrice
    private OrderItemStatus status;
    private String customName; // For custom pizzas
    private String customDescription; // For custom pizzas
    private List<OrderItemIngredientDto> ingredients; // All ingredients (base, added, removed)
    private boolean isCustom; // true if productId is null
}

