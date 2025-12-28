package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(name = "quantity_change")
    private BigDecimal quantityChange;

    @Column(name = "movement_type")
    @Enumerated(EnumType.STRING)
    private MovementType movementType; // USE, RESTOCK, ADJUSTMENT

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
