package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPromotionsId implements Serializable {
    private Integer orderId;
    private Integer promotionId;
}