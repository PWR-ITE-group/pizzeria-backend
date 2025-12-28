package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AddIngredientRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.BatchIngredientsRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductIngredientDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.ProductIngredientService;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductIngredientService productIngredientService;

    public ProductController(ProductService productService, ProductIngredientService productIngredientService) {
        this.productService = productService;
        this.productIngredientService = productIngredientService;
    }

    // 1. Создать продукт в каталоге (Без меню)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.createProductInCatalog(dto));
    }

    // 2. Получить список "свободных" продуктов (для выпадающего списка в админке)
    @Deprecated
    @GetMapping("/catalog")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductDto>> getCatalogProducts() {
        return ResponseEntity.ok(productService.getProductsWithoutMenu());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProductsWithStatus());
    }

    // 3. Назначить продукт в меню
    // PUT /api/products/{id}/assign-menu/{menuId}
    @PutMapping("/{id}/assign-menu/{menuId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> assignToMenu(@PathVariable Long id, @PathVariable Long menuId) {
        productService.assignProductToMenu(id, menuId);
        return ResponseEntity.ok().build();
    }

    // 4. Обновить продукт
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    // ===== INGREDIENT MANAGEMENT ENDPOINTS =====

    // 5. Get all ingredients for a product
    @GetMapping("/{id}/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductIngredientDto>> getProductIngredients(@PathVariable Long id) {
        return ResponseEntity.ok(productIngredientService.getIngredientsForProduct(id));
    }

    // 6. Add single ingredient to a product
    @PostMapping("/{id}/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductIngredientDto> addIngredient(
            @PathVariable Long id,
            @RequestBody AddIngredientRequest request) {
        ProductIngredientDto result = productIngredientService.addIngredientToProduct(
                id, request.getIngredientId(), request.getQuantity());
        return ResponseEntity.ok(result);
    }

    // 7. Add multiple ingredients to a product (batch)
    @PostMapping("/{id}/ingredients/batch")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductIngredientDto>> addIngredientsBatch(
            @PathVariable Long id,
            @RequestBody BatchIngredientsRequest request) {
        List<ProductIngredientDto> result = productIngredientService.addIngredientsToProduct(
                id, request.getIngredients());
        return ResponseEntity.ok(result);
    }

    // 8. Update ingredient quantity in a product
    @PutMapping("/{id}/ingredients/{ingredientId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductIngredientDto> updateIngredientQuantity(
            @PathVariable Long id,
            @PathVariable Long ingredientId,
            @RequestBody AddIngredientRequest request) {
        ProductIngredientDto result = productIngredientService.updateIngredientQuantity(
                id, ingredientId, request.getQuantity());
        return ResponseEntity.ok(result);
    }

    // 9. Remove ingredient from a product
    @DeleteMapping("/{id}/ingredients/{ingredientId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeIngredient(
            @PathVariable Long id,
            @PathVariable Long ingredientId) {
        productIngredientService.removeIngredientFromProduct(id, ingredientId);
        return ResponseEntity.ok().build();
    }

    // 10. Replace all ingredients for a product
    @PutMapping("/{id}/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductIngredientDto>> replaceAllIngredients(
            @PathVariable Long id,
            @RequestBody BatchIngredientsRequest request) {
        List<ProductIngredientDto> result = productIngredientService.replaceAllIngredients(
                id, request.getIngredients());
        return ResponseEntity.ok(result);
    }
}
