package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_info", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private String name;

    @Column(name = "last_name")
    private String lastName;

    private String phone;
    private String email;
    private String street;

    @Column(name = "house_nr")
    private String houseNr;

    @Column(name = "apartment_nr")
    private String apartmentNr;

    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "additional_info")
    private String additionalInfo;
}

