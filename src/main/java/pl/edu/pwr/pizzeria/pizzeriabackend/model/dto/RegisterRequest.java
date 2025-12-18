package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;        // [cite: 545]
    private String lastName;    // [cite: 549]
    private String phone;       // [cite: 552]
    private String login;       // [cite: 556]
    private String password;    // [cite: 560]
    private String role;        // [cite: 564] (waiter, chef, manager, courier)
}
