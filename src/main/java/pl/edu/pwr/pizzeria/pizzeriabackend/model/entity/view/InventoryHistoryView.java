package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "inventory_history_view", schema = "pizzeria_schema")
@Data
public class InventoryHistoryView {

    @Id
    private Long id;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "ingredient_name")
    private String ingredientName;

    @Column(name = "quantity_change")
    private BigDecimal quantityChange;

    @Column(name = "movement_type")
    private String movementType;

    private LocalDateTime timestamp;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "employee_last_name")
    private String employeeLastName;
}