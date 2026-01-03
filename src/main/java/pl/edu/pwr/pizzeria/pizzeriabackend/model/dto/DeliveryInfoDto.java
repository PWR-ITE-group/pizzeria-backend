package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfoDto {
    private Long id;
    private String name;
    private String lastName;
    private String phone;
    private String email;
    private String street;
    private String houseNr;
    private String apartmentNr;
    private String city;
    private String postalCode;
    private String additionalInfo;
}

