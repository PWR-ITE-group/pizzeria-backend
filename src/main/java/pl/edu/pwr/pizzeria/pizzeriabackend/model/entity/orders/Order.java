package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.converter.OrderStatusConverter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.converter.OrderTypeConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus status;

    @Column(name = "order_type")
    @Convert(converter = OrderTypeConverter.class)
    private OrderType orderType;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Поле из скрипта V5 (авто-расчет суммы)
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}
