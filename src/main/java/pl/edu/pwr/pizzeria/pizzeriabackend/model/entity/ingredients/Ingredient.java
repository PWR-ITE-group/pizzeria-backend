package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.IngredientUnit;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredients", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private IngredientUnit unit;

    @Column(name = "stock_quantity")
    private BigDecimal stockQuantity;
}
