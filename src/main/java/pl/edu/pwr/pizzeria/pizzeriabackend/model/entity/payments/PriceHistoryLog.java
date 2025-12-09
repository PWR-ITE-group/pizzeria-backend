package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history_log", schema = "pizzeria_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "old_price")
    private BigDecimal oldPrice;

    @Column(name = "new_price")
    private BigDecimal newPrice;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy;
}