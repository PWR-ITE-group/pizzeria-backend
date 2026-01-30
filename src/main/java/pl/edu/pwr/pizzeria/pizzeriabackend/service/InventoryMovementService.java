package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AdjustmentRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.InventoryMovementDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.RestockRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.InventoryMovement;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.MovementType;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.IngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.InventoryMovementRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final IngredientRepository ingredientRepository;
    private final EmployeeRepository employeeRepository;

    public InventoryMovementService(
            InventoryMovementRepository inventoryMovementRepository,
            IngredientRepository ingredientRepository,
            EmployeeRepository employeeRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.ingredientRepository = ingredientRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Restock an ingredient (add stock).
     * DB trigger will automatically update the ingredient's stock_quantity.
     */
    @Transactional
    public InventoryMovementDto restockIngredient(Long ingredientId, BigDecimal quantity, Long employeeId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Restock quantity must be positive");
        }

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        InventoryMovement movement = InventoryMovement.builder()
                .ingredient(ingredient)
                .quantityChange(quantity)
                .movementType(MovementType.RESTOCK)
                .timestamp(LocalDateTime.now())
                .employee(employee)
                .build();

        InventoryMovement saved = inventoryMovementRepository.save(movement);

        return mapToDto(saved);
    }

    /**
     * Manually adjust inventory (positive or negative).
     * DB trigger will automatically update the ingredient's stock_quantity.
     */
    @Transactional
    public InventoryMovementDto adjustInventory(Long ingredientId, BigDecimal quantity, String reason, Long employeeId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Adjustment quantity cannot be zero");
        }

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        BigDecimal newStock = ingredient.getStockQuantity().add(quantity);
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Adjustment would result in negative stock. Current: " + ingredient.getStockQuantity() +
                    ", Change: " + quantity + ", Result: " + newStock);
        }

        InventoryMovement movement = InventoryMovement.builder()
                .ingredient(ingredient)
                .quantityChange(quantity)
                .movementType(MovementType.ADJUSTMENT)
                .timestamp(LocalDateTime.now())
                .employee(employee)
                .build();

        InventoryMovement saved = inventoryMovementRepository.save(movement);

        return mapToDto(saved);
    }

    /**
     * Get all movement history for a specific ingredient.
     */
    @Transactional(readOnly = true)
    public List<InventoryMovementDto> getMovementHistory(Long ingredientId) {
        if (!ingredientRepository.existsById(ingredientId)) {
            throw new RuntimeException("Ingredient not found with id: " + ingredientId);
        }

        return inventoryMovementRepository.findByIngredientIdOrderByTimestampDesc(ingredientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    /**
     * Get all movements across all ingredients.
     */
    @Transactional(readOnly = true)
    public List<InventoryMovementDto> getAllMovements() {
        return inventoryMovementRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get movements filtered by type.
     */
    @Transactional(readOnly = true)
    public List<InventoryMovementDto> getMovementsByType(String typeString) {
        MovementType type = MovementType.fromString(typeString);
        
        return inventoryMovementRepository.findAll().stream()
                .filter(m -> m.getMovementType() == type)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get movements performed by a specific employee.
     */
    @Transactional(readOnly = true)
    public List<InventoryMovementDto> getMovementsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Employee not found with id: " + employeeId);
        }

        return inventoryMovementRepository.findAll().stream()
                .filter(m -> m.getEmployee() != null && m.getEmployee().getId().equals(employeeId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    private InventoryMovementDto mapToDto(InventoryMovement movement) {
        return InventoryMovementDto.builder()
                .id(movement.getId())
                .ingredientId(movement.getIngredient().getId())
                .ingredientName(movement.getIngredient().getName())
                .quantityChange(movement.getQuantityChange())
                .movementType(movement.getMovementType())
                .timestamp(movement.getTimestamp())
                .employeeId(movement.getEmployee() != null ? movement.getEmployee().getId() : null)
                .employeeName(movement.getEmployee() != null ?
                        movement.getEmployee().getName() + " " + movement.getEmployee().getLastName() : null)
                .build();
    }
}

