package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menus", schema = "pizzeria_schema")
@Data
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;
}
