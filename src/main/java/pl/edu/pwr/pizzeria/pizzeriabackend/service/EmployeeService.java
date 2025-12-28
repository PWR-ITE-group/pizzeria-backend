package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateEmployeeRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.EmployeeDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateEmployeeRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all employees in the system.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific employee by ID.
     */
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    /**
     * Create a new employee with password hashing.
     */
    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        // Validate required fields
        validateCreateRequest(request);

        // Check for duplicate login
        if (employeeRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Login already exists: " + request.getLogin());
        }

        // Check for duplicate phone
        if (employeeRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists: " + request.getPhone());
        }

        // Validate role
        validateRole(request.getRole());

        // Validate password strength
        validatePassword(request.getPassword());

        // Create employee entity
        Employee employee = Employee.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .login(request.getLogin())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().toUpperCase())
                .createdAt(LocalDateTime.now())
                .completedOrdersCount(0)
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    /**
     * Update employee information (NOT password).
     */
    @Transactional
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            employee.setName(request.getName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            employee.setLastName(request.getLastName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            // Check if phone is already taken by another employee
            if (employeeRepository.existsByPhone(request.getPhone())) {
                Employee existingEmployee = employeeRepository.findAll().stream()
                        .filter(e -> e.getPhone().equals(request.getPhone()))
                        .findFirst()
                        .orElse(null);
                if (existingEmployee != null && !existingEmployee.getId().equals(id)) {
                    throw new RuntimeException("Phone number already exists: " + request.getPhone());
                }
            }
            employee.setPhone(request.getPhone());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            validateRole(request.getRole());
            employee.setRole(request.getRole().toUpperCase());
        }

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    /**
     * Delete an employee from the system.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    // ===== PHASE 2: ROLE-BASED OPERATIONS =====

    /**
     * Get all employees with a specific role.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByRole(String role) {
        validateRole(role);
        return employeeRepository.findByRole(role.toUpperCase()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all couriers (for delivery assignment).
     */
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAvailableCouriers() {
        return employeeRepository.findByRole("COURIER").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all chefs (for kitchen view).
     */
    @Transactional(readOnly = true)
    public List<EmployeeDto> getChefs() {
        return employeeRepository.findByRole("CHEF").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Validation Methods ---

    private void validateCreateRequest(CreateEmployeeRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new RuntimeException("Last name is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new RuntimeException("Phone is required");
        }
        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new RuntimeException("Login is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("Role is required");
        }
    }

    private void validateRole(String role) {
        String upperRole = role.toUpperCase();
        if (!upperRole.equals("MANAGER") && !upperRole.equals("CHEF") &&
            !upperRole.equals("WAITER") && !upperRole.equals("COURIER")) {
            throw new RuntimeException("Invalid role. Must be one of: MANAGER, CHEF, WAITER, COURIER");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
    }

    // --- Mapper ---

    private EmployeeDto mapToDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .lastName(employee.getLastName())
                .phone(employee.getPhone())
                .login(employee.getLogin())
                .role(employee.getRole())
                .createdAt(employee.getCreatedAt())
                .completedOrdersCount(employee.getCompletedOrdersCount())
                .build();
    }
}

