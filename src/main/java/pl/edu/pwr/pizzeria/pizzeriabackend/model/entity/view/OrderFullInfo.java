package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "order_full_info", schema = "pizzeria_schema")
@Data
public class OrderFullInfo {
    @Id
    private Long orderId; // View должен иметь ID
    private String orderStatus;
    private String orderType;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
    private String employeeName;
    private String employeeLastName;
    private Long deliveryId;
    private String courierName;
    private String customerPhone;
    private String city;
    private String street;
}