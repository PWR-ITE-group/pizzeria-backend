package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AuthResponse; // Stwórz to DTO
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.LoginRequest; // Stwórz to DTO
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.security.JwtService; // Zaraz to napiszemy

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Spring Security sprawdza login i hasło
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );

        // 2. Jeśli przeszło, szukamy użytkownika w bazie, żeby pobrać jego rolę
        var employee = employeeRepository.findByLogin(request.getLogin())
                .orElseThrow();

        // 3. Generujemy token
        String token = jwtService.generateToken(employee);

        return new AuthResponse(token, employee.getRole());
    }

    // Metoda do rejestracji (dla Managera)
    // ...
}
