package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private OrderStatus status;
    private OrderType orderType;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalPrice;
    private List<OrderItemDto> items;  // Will be empty in Phase 1
}

