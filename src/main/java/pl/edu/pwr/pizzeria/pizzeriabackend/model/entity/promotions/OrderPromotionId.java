package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPromotionId implements Serializable {
    private Long order;
    private Long promotion;
}

