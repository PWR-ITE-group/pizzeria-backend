package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;

@Entity
@Table(name = "order_promotions", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderPromotionId.class)
public class OrderPromotion {
    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Id
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
}

