package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {
    private String name;
    private String lastName;
    private String phone;
    private String role;  // MANAGER, CHEF, WAITER, COURIER
    // NOTE: Password is NOT updated here - use separate password change endpoint
}

