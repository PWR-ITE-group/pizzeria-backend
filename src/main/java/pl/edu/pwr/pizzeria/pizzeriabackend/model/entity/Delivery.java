package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries", schema = "pizzeria_schema")
@Getter
@Setter
@NoArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Employee employee;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @OneToOne(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private DeliveryInfo deliveryInfo;

    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
        if (deliveryInfo != null) {
            deliveryInfo.setDelivery(this);
        }
    }
}
