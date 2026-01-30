package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentCompanyDetailsRequest {
    private String companyName;
    private String nip;
    private String street;
    private String houseNr;
    private String city;
    private String postalCode;
    private String accountNumber;
}

