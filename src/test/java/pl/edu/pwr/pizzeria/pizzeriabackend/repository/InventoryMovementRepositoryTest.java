package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.pwr.pizzeria.pizzeriabackend.AbstractIntegrationTest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.InventoryMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class InventoryMovementRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    IngredientRepository ingredientRepository;


    @Test
    public void testInventoryMovementRepository() {
        Ingredient cheese = new Ingredient();
        cheese.setName("Mozzarella");
        cheese.setUnit("grams");
        cheese.setStockQuantity(new BigDecimal("1000.00"));
        ingredientRepository.save(cheese);

        Employee courier = new Employee();
        courier.setName("John");
        courier.setLastName("Doe");
        courier.setPhone("1234567890");
        courier.setLogin("johndoe");
        courier.setPasswordHash("secret");
        courier.setRole("courier");
        courier.setCreatedAt(LocalDateTime.now());
        employeeRepository.save(courier);

        InventoryMovement movement = new InventoryMovement();
        movement.setIngredient(cheese);
        movement.setQuantityChange(new BigDecimal("200.00"));
        movement.setMovementType(new String("Mozzarella"));
        movement.setTimestamp(LocalDateTime.now());
        movement.setEmployee(courier);

        inventoryMovementRepository.save(movement);

        assertThat(courier.getId()).isNotNull();
        assertThat(courier.getName()).isEqualTo("John");

        assertThat(cheese.getId()).isNotNull();
        assertThat(cheese.getName()).isEqualTo("Mozzarella");
        assertThat(cheese.getUnit()).isEqualTo("grams");
        assertThat(cheese.getStockQuantity()).isEqualByComparingTo("1000.00");

        assertThat(movement.getId()).isNotNull();
        assertThat(movement.getMovementType()).isEqualTo("Mozzarella");
        assertThat(movement.getTimestamp()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));        assertThat(movement.getEmployee()).isEqualTo(courier);
        assertThat(movement.getIngredient()).isEqualTo(cheese);
    }
}
