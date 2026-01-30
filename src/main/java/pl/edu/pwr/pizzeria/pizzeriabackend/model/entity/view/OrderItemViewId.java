package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class OrderItemViewId implements Serializable {
    private Long orderId;
    private Long productId;
}