package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_company_details", schema = "pizzeria_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompanyDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String nip;

    @Column
    private String street;

    @Column(name = "house_nr")
    private String houseNr;

    @Column
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "account_number")
    private String accountNumber;
}

