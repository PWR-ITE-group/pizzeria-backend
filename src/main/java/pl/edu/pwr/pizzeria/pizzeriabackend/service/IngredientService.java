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

    @Transactional(readOnly = true)
    public List<IngredientDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IngredientDto> getPublicIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        ingredient.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : BigDecimal.ZERO);

        Ingredient saved = ingredientRepository.save(ingredient);
        return mapToDto(saved);
    }

    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Składnik nie znaleziony"));

        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());


        Ingredient saved = ingredientRepository.save(ingredient);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Składnik nie znaleziony");
        }
        ingredientRepository.deleteById(id);
    }

    private IngredientDto mapToDto(Ingredient ingredient) {
        IngredientStockStatus status = IngredientStockStatus.OK;

        if (ingredient.getStockQuantity().compareTo(BigDecimal.valueOf(10)) < 0) {
            status = IngredientStockStatus.LOW;
        }

        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .unit(ingredient.getUnit())
                .stockQuantity(ingredient.getStockQuantity())
                .stockStatus(status)
                .build();
    }
}
