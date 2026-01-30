package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "delivery_status_view", schema = "pizzeria_schema")
@Data
public class DeliveryStatusView {

    @Id
    @Column(name = "delivery_id")
    private Long deliveryId;

    private String status;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_status")
    private String orderStatus; // Статус самого заказа

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "courier_last_name")
    private String courierLastName;

    private String city;

    private String street;

    @Column(name = "house_nr")
    private String houseNr;
}