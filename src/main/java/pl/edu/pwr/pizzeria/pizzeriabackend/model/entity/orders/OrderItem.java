package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.converter.OrderItemStatusConverter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "order_items", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Convert(converter = OrderItemStatusConverter.class)
    private OrderItemStatus status;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "custom_description")
    private String customDescription;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemIngredient> orderItemIngredients;
}
