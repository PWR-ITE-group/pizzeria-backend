package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AdjustmentRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.InventoryMovementDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.RestockRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.InventoryMovementService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;
    private final EmployeeRepository employeeRepository;

    public InventoryMovementController(InventoryMovementService inventoryMovementService, 
                                      EmployeeRepository employeeRepository) {
        this.inventoryMovementService = inventoryMovementService;
        this.employeeRepository = employeeRepository;
    }

    // ===== PHASE 1: MANUAL STOCK OPERATIONS =====

    // 1. Restock ingredient (add stock)
    // CHEF can restock when receiving deliveries, MANAGER for any reason
    @PostMapping("/restock")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<InventoryMovementDto> restockIngredient(
            @RequestBody RestockRequest request,
            Authentication authentication) {
        // Automatically get employee ID from authenticated user
        Long employeeId = getEmployeeIdFromAuth(authentication);
        
        InventoryMovementDto result = inventoryMovementService.restockIngredient(
                request.getIngredientId(),
                request.getQuantity(),
                employeeId
        );
        return ResponseEntity.ok(result);
    }

    // 2. Manual inventory adjustment (correction)
    // Only MANAGER can make corrections (positive or negative adjustments)
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<InventoryMovementDto> adjustInventory(
            @RequestBody AdjustmentRequest request,
            Authentication authentication) {
        // Automatically get employee ID from authenticated user
        Long employeeId = getEmployeeIdFromAuth(authentication);
        
        InventoryMovementDto result = inventoryMovementService.adjustInventory(
                request.getIngredientId(),
                request.getQuantity(),
                request.getReason(),
                employeeId
        );
        return ResponseEntity.ok(result);
    }

    // 3. Get movement history for specific ingredient
    @GetMapping("/movements/{ingredientId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<List<InventoryMovementDto>> getMovementHistory(@PathVariable Long ingredientId) {
        return ResponseEntity.ok(inventoryMovementService.getMovementHistory(ingredientId));
    }

    // ===== PHASE 2: MOVEMENT HISTORY & FILTERING =====

    // 4. Get all movements across all ingredients
    @GetMapping("/movements")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<InventoryMovementDto>> getAllMovements() {
        return ResponseEntity.ok(inventoryMovementService.getAllMovements());
    }

    // 5. Get movements by type (use, restock, adjustment)
    @GetMapping("/movements/type/{type}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<List<InventoryMovementDto>> getMovementsByType(@PathVariable String type) {
        return ResponseEntity.ok(inventoryMovementService.getMovementsByType(type));
    }

    // 6. Get movements by employee
    @GetMapping("/movements/employee/{employeeId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<InventoryMovementDto>> getMovementsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(inventoryMovementService.getMovementsByEmployee(employeeId));
    }

    // Helper method to extract employee ID from authentication
    private Long getEmployeeIdFromAuth(Authentication authentication) {
        String login = authentication.getName();
        return employeeRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Employee not found"))
                .getId();
    }
}

