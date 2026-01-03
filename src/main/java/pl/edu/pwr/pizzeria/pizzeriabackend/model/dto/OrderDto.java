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
    
    // Payment information
    private String paymentStatus;  // "none", "pending", "paid", "failed"
    private Long paymentId;        // ID платежа, если существует
    private String paymentMethod; // метод оплаты, если оплачено
    
    // Promotion information
    private List<PromotionDto> appliedPromotions; // примененные промо-коды
    private BigDecimal discountAmount;           // сумма скидки
    private BigDecimal finalPrice;                // итоговая цена после скидки
    
    // Tracking information
    private String trackingToken;                 // уникальный токен для отслеживания
    private String trackingUrl;                   // полная ссылка для отслеживания заказа
}

