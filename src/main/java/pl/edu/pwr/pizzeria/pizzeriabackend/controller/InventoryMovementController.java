package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Inventory", description = "Inventory management endpoints for managing stock movements, restocking, and adjustments")
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
    @Operation(
            summary = "Restock ingredient",
            description = "Add stock to an ingredient. Manager and Chef access.",
            tags = {"Inventory"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient restocked successfully",
                    content = @Content(schema = @Schema(implementation = InventoryMovementDto.class))),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Adjust inventory",
            description = "Manually adjust inventory quantity (correction). Manager access only.",
            tags = {"Inventory"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory adjusted successfully",
                    content = @Content(schema = @Schema(implementation = InventoryMovementDto.class))),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Get ingredient movement history",
            description = "Retrieve movement history for a specific ingredient. Manager and Chef access.",
            tags = {"Inventory"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movement history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/movements/{ingredientId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<List<InventoryMovementDto>> getMovementHistory(
            @Parameter(description = "Ingredient ID") @PathVariable Long ingredientId) {
        return ResponseEntity.ok(inventoryMovementService.getMovementHistory(ingredientId));
    }

    // ===== PHASE 2: MOVEMENT HISTORY & FILTERING =====

    // 4. Get all movements across all ingredients
    @Operation(
            summary = "Get all inventory movements",
            description = "Retrieve all inventory movements across all ingredients. Manager access only.",
            tags = {"Inventory"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movements retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/movements")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<InventoryMovementDto>> getAllMovements() {
        return ResponseEntity.ok(inventoryMovementService.getAllMovements());
    }

    // 5. Get movements by type (use, restock, adjustment)
    @Operation(
            summary = "Get movements by type",
            description = "Retrieve inventory movements filtered by type (use, restock, adjustment). Manager and Chef access.",
            tags = {"Inventory"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movements retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid movement type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/movements/type/{type}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<List<InventoryMovementDto>> getMovementsByType(
            @Parameter(description = "Movement type") @PathVariable String type) {
        return ResponseEntity.ok(inventoryMovementService.getMovementsByType(type));
    }

    // 6. Get movements by employee
    @Operation(
            summary = "Get movements by employee",
            description = "Retrieve inventory movements filtered by employee. Manager access only.",
            tags = {"Inventory"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movements retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/movements/employee/{employeeId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<InventoryMovementDto>> getMovementsByEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
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

