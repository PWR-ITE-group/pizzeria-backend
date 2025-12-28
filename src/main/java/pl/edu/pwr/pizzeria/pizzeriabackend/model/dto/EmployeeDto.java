package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String name;
    private String lastName;
    private String phone;
    private String login;
    private String role;
    private LocalDateTime createdAt;
    private Integer completedOrdersCount;
    // NOTE: passwordHash is NEVER included in DTOs for security reasons
}

