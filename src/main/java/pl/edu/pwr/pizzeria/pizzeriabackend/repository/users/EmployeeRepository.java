package pl.edu.pwr.pizzeria.pizzeriabackend.repository.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;

import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByLogin(String login);

    boolean existsByLogin(String login);
    boolean existsByPhone(String phone);

    List<Employee> findByRole(String role);
}
