package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
    private Long id;
    private Long orderId;
    private Long courierId;
    private String courierName;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
    private DeliveryInfoDto deliveryInfo;
}

