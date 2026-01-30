package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemStatusRequest {
    private OrderItemStatus status;
}

