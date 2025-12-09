package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.Payment;

@Entity
@Table(name = "payment_company_details", schema = "pizzeria_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompanyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Владелец связи (здесь находится внешний ключ payment_id)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true, nullable = false)
    private Payment payment;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false, length = 20)
    private String nip;

    private String street;

    @Column(name = "house_nr", length = 10)
    private String houseNr;

    @Column(length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "account_number", length = 50)
    private String accountNumber;
}