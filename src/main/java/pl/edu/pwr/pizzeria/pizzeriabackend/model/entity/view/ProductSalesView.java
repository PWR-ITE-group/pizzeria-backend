package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "product_sales_view", schema = "pizzeria_schema")
@Data
public class ProductSalesView {
    @Id
    private Long productId;
    private String name;
    private Long totalSold;
    private BigDecimal totalRevenue;
}