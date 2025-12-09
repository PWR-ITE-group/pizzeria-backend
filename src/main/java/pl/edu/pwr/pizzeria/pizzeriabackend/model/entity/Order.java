package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", schema = "pizzeria_schema")
@Getter @Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Delivery delivery;

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        if (delivery == null) {
            if (this.delivery != null) {
                this.delivery.setOrder(null);
            }
        } else {
            delivery.setOrder(this);
        }
        this.delivery = delivery;
    }

}
