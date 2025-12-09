package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "order_items_view", schema = "pizzeria_schema")
@Data
@IdClass(OrderItemViewId.class)
public class OrderItemsInfoView {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private String status;
}