package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AddIngredientRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductIngredientDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredientId;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.IngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.ProductIngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductIngredientService {

    private final ProductIngredientRepository productIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;

    public ProductIngredientService(
            ProductIngredientRepository productIngredientRepository,
            IngredientRepository ingredientRepository,
            ProductRepository productRepository) {
        this.productIngredientRepository = productIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.productRepository = productRepository;
    }

    /**
     * Add a single ingredient to a product.
     * If the ingredient already exists, updates its quantity.
     */
    @Transactional
    public ProductIngredientDto addIngredientToProduct(Long productId, Long ingredientId, BigDecimal quantity) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientId));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        ProductIngredient productIngredient = new ProductIngredient();
        productIngredient.setProductId(productId);
        productIngredient.setIngredientId(ingredientId);
        productIngredient.setQuantity(quantity);

        ProductIngredient saved = productIngredientRepository.save(productIngredient);

        return mapToDto(saved, ingredient);
    }

    /**
     * Add multiple ingredients to a product in batch.
     */
    @Transactional
    public List<ProductIngredientDto> addIngredientsToProduct(Long productId, List<AddIngredientRequest> ingredients) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        List<ProductIngredientDto> result = new ArrayList<>();
        for (AddIngredientRequest request : ingredients) {
            ProductIngredientDto dto = addIngredientToProduct(productId, request.getIngredientId(), request.getQuantity());
            result.add(dto);
        }
        return result;
    }

    /**
     * Update the quantity of an ingredient in a product.
     */
    @Transactional
    public ProductIngredientDto updateIngredientQuantity(Long productId, Long ingredientId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        ProductIngredientId id = new ProductIngredientId(productId, ingredientId);
        ProductIngredient productIngredient = productIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product ingredient relationship not found"));

        productIngredient.setQuantity(quantity);
        ProductIngredient saved = productIngredientRepository.save(productIngredient);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        return mapToDto(saved, ingredient);
    }

    /**
     * Remove an ingredient from a product.
     */
    @Transactional
    public void removeIngredientFromProduct(Long productId, Long ingredientId) {
        ProductIngredientId id = new ProductIngredientId(productId, ingredientId);
        if (!productIngredientRepository.existsById(id)) {
            throw new RuntimeException("Product ingredient relationship not found");
        }
        productIngredientRepository.deleteById(id);
    }

    /**
     * Get all ingredients for a specific product.
     */
    @Transactional(readOnly = true)
    public List<ProductIngredientDto> getIngredientsForProduct(Long productId) {
        List<ProductIngredient> productIngredients = productIngredientRepository.findByProductId(productId);
        
        return productIngredients.stream()
                .map(pi -> {
                    Ingredient ingredient = ingredientRepository.findById(pi.getIngredientId())
                            .orElse(null);
                    return ingredient != null ? mapToDto(pi, ingredient) : null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Replace all ingredients for a product with a new list.
     * This removes all existing ingredients and adds the new ones.
     */
    @Transactional
    public List<ProductIngredientDto> replaceAllIngredients(Long productId, List<AddIngredientRequest> ingredients) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        List<ProductIngredient> existingIngredients = productIngredientRepository.findByProductId(productId);
        productIngredientRepository.deleteAll(existingIngredients);

        if (ingredients == null || ingredients.isEmpty()) {
            return new ArrayList<>();
        }

        return addIngredientsToProduct(productId, ingredients);
    }

    /**
     * Map ProductIngredient entity to DTO.
     */
    private ProductIngredientDto mapToDto(ProductIngredient productIngredient, Ingredient ingredient) {
        return ProductIngredientDto.builder()
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .quantity(productIngredient.getQuantity())
                .unit(ingredient.getUnit())
                .build();
    }
}

