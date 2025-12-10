package pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", schema = "pizzeria_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; // waiter, chef, manager, courier

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_orders_count")
    private Integer completedOrdersCount;
}
