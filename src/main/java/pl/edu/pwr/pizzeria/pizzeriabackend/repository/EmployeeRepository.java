package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Ingredient;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
