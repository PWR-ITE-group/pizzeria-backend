package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.IngredientDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.IngredientService;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    // UC11: Podgląd magazynu (Dostępne dla Managera i Kucharza)
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    // UC11: Dodawanie nowego składnika (Tylko Manager)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<IngredientDto> createIngredient(@RequestBody IngredientDto dto) {
        return ResponseEntity.ok(ingredientService.createIngredient(dto));
    }

    // UC11: Korekta/Edycja składnika (Tylko Manager)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<IngredientDto> updateIngredient(@PathVariable Long id, @RequestBody IngredientDto dto) {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, dto));
    }

    // Usuwanie składnika z systemu (Tylko Manager)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.ok().build();
    }
}
