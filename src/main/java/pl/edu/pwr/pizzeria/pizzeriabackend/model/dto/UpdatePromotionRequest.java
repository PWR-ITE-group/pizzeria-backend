package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePromotionRequest {
    private String description;
    private BigDecimal discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
}

