package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "product_ingredients", schema = "pizzeria_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductIngredientId.class) // Составной ключ
public class ProductIngredient {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Id
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", insertable = false, updatable = false)
    private Ingredient ingredient;

    private BigDecimal quantity;
}
