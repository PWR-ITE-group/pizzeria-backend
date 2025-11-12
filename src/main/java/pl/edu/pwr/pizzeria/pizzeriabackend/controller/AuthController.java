package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AuthRequestDTO;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.EmployeeDetailsDTO;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    // @Autowired AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequestDTO request) {
        // TODO: Autentykacja (Spring Security), generowanie JWT na podstawie loginu i hasła
        return ResponseEntity.ok("JWT_TOKEN_HERE");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Dostępny dla każdego zalogowanego pracownika
    public ResponseEntity<EmployeeDetailsDTO> getMyProfile() {
        // TODO: Pobranie danych pracownika na podstawie JWT (Principal)
        return ResponseEntity.ok(new EmployeeDetailsDTO());
    }
}