package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;

@Entity
@Table(name = "order_promotions", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderPromotionsId.class) // Подключаем составной ключ
public class OrderPromotions {

    // 1. Поля ключа
    @Id
    @Column(name = "order_id")
    private Integer orderId;

    @Id
    @Column(name = "promotion_id")
    private Integer promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private Promotions promotion;
}