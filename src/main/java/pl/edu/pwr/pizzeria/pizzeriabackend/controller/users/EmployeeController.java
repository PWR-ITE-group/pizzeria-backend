package pl.edu.pwr.pizzeria.pizzeriabackend.controller.users;

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
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateEmployeeRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.EmployeeDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateEmployeeRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.EmployeeService;

import java.util.List;

@Tag(name = "Employees", description = "Employee management endpoints for managing employees, roles, and employee information")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(
            summary = "Get all employees",
            description = "Retrieve all employees in the system. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Operation(
            summary = "Get employee by ID",
            description = "Retrieve a specific employee by ID. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeDto> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(
            summary = "Create employee",
            description = "Create a new employee in the system. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @Operation(
            summary = "Update employee",
            description = "Update employee information. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @Operation(
            summary = "Delete employee",
            description = "Delete an employee from the system. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get employees by role",
            description = "Retrieve all employees with a specific role. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByRole(
            @Parameter(description = "Employee role") @PathVariable String role) {
        return ResponseEntity.ok(employeeService.getEmployeesByRole(role));
    }

    @Operation(
            summary = "Get available couriers",
            description = "Retrieve all employees with courier role for delivery assignment. Manager access only.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Couriers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/couriers")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getAvailableCouriers() {
        return ResponseEntity.ok(employeeService.getAvailableCouriers());
    }

    @Operation(
            summary = "Get all chefs",
            description = "Retrieve all employees with chef role. Manager and Waiter access.",
            tags = {"Employees"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chefs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/chefs")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<List<EmployeeDto>> getChefs() {
        return ResponseEntity.ok(employeeService.getChefs());
    }
}

