package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredient;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private String name;
    private String description;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available")
    private Boolean isAvailable;

    // Связь с ингредиентами через промежуточную таблицу
    @OneToMany(mappedBy = "product")
    private List<ProductIngredient> productIngredients;
}