package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.ModificationType;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.converter.ModificationTypeConverter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item_ingredients", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private BigDecimal quantity;

    @Column(name = "modification_type")
    @Convert(converter = ModificationTypeConverter.class)
    private ModificationType modificationType;
}

