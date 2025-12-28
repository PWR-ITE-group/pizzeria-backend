package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.IngredientDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.IngredientStockStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.IngredientRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    // UC11: Pobranie stanu magazynowego
    @Transactional(readOnly = true)
    public List<IngredientDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // UC11: Dodanie nowego składnika
    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit()); // Teraz przyjmujemy Enum
        ingredient.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : BigDecimal.ZERO);

        Ingredient saved = ingredientRepository.save(ingredient);
        return mapToDto(saved);
    }

    // UC11: Aktualizacja danych składnika
    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Składnik nie znaleziony"));

        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit()); // Enum

        if (dto.getStockQuantity() != null) {
            ingredient.setStockQuantity(dto.getStockQuantity());
        }

        Ingredient saved = ingredientRepository.save(ingredient);
        return mapToDto(saved);
    }

    // Usuwanie składnika
    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Składnik nie znaleziony");
        }
        ingredientRepository.deleteById(id);
    }

    // --- Mapper z logiką statusu ---
    private IngredientDto mapToDto(Ingredient ingredient) {
        // Logika z SQL View: stock_quantity < 10 THEN 'LOW' ELSE 'OK'
        IngredientStockStatus status = IngredientStockStatus.OK;

        // Jeśli ilość < 10, ustawiamy status na LOW
        if (ingredient.getStockQuantity().compareTo(BigDecimal.valueOf(10)) < 0) {
            status = IngredientStockStatus.LOW;
        }

        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .unit(ingredient.getUnit()) // Przekazujemy Enum
                .stockQuantity(ingredient.getStockQuantity())
                .stockStatus(status)        // Wyliczony Enum
                .build();
    }
}
