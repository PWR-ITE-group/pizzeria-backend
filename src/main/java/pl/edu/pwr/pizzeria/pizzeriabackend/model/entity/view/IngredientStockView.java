package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "ingredient_stock_view", schema = "pizzeria_schema")
@Data
public class IngredientStockView {
    @Id
    private Long id;
    private String name;
    private String unit;
    private BigDecimal stockQuantity;
    private String stockStatus;
}