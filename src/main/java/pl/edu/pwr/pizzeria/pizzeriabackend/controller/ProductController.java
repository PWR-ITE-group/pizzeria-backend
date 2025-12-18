package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 1. Создать продукт в каталоге (Без меню)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.createProductInCatalog(dto));
    }

    // 2. Получить список "свободных" продуктов (для выпадающего списка в админке)
    @GetMapping("/catalog")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductDto>> getCatalogProducts() {
        return ResponseEntity.ok(productService.getProductsWithoutMenu());
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
}
