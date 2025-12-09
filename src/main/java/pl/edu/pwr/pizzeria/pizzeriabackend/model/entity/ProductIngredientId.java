package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientId implements Serializable {
    private Integer productId;
    private Integer ingredientId;
}