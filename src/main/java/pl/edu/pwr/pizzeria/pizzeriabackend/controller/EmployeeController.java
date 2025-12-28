package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateEmployeeRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.EmployeeDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateEmployeeRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ===== PHASE 1: BASIC CRUD =====

    // 1. Get all employees
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // 2. Get employee by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // 3. Create new employee
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    // 4. Update employee information
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // 5. Delete employee
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

    // ===== PHASE 2: ROLE-BASED OPERATIONS =====

    // 6. Get employees by role
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByRole(@PathVariable String role) {
        return ResponseEntity.ok(employeeService.getEmployeesByRole(role));
    }

    // 7. Get all couriers (for delivery assignment)
    @GetMapping("/couriers")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getAvailableCouriers() {
        return ResponseEntity.ok(employeeService.getAvailableCouriers());
    }

    // 8. Get all chefs (for kitchen view)
    @GetMapping("/chefs")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<List<EmployeeDto>> getChefs() {
        return ResponseEntity.ok(employeeService.getChefs());
    }
}

