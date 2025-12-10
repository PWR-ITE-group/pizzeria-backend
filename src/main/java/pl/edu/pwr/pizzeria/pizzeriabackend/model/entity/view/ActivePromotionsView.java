package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Immutable
@Table(name = "active_promotions", schema = "pizzeria_schema")
@Data
public class ActivePromotionsView {

    @Id
    private Long id;
    private String code;
    private String description;
    private BigDecimal discount_percent;
    private Date valid_from;
    private Date valid_to;
    private Boolean is_active;
}
