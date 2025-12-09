package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Employee courier;

    private String status; // assigned, in_transit...

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}