package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", schema = "pizzeria_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available" )
    private Boolean isAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductIngredient> productIngredients = new ArrayList<>();

    public void addIngredient(Ingredient ingredient, BigDecimal quantity) {
        ProductIngredient link = new ProductIngredient();
        link.setProduct(this);
        link.setIngredient(ingredient);
        link.setQuantity(quantity);

        this.productIngredients.add(link);
    }
}
