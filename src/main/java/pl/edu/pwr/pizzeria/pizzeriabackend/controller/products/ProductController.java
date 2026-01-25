package pl.edu.pwr.pizzeria.pizzeriabackend.controller.products;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Products", description = "Product management endpoints for managing products, ingredients, and product-ingredient relationships")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductIngredientService productIngredientService;

    public ProductController(ProductService productService, ProductIngredientService productIngredientService) {
        this.productService = productService;
        this.productIngredientService = productIngredientService;
    }

    @Operation(
            summary = "Create product",
            description = "Create a new product in the catalog (without assigning to a menu). Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.createProductInCatalog(dto));
    }

    @Deprecated
    @Operation(
            summary = "Get catalog products (deprecated)",
            description = "Get products without menu assignment. Deprecated - use /all instead. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/catalog")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductDto>> getCatalogProducts() {
        return ResponseEntity.ok(productService.getProductsWithoutMenu());
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieve all products with their status (assigned/unassigned to menu). Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProductsWithStatus());
    }

    @Operation(
            summary = "Assign product to menu",
            description = "Assign a product to a menu. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product assigned to menu successfully"),
            @ApiResponse(responseCode = "404", description = "Product or menu not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/assign-menu/{menuId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> assignToMenu(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Menu ID") @PathVariable Long menuId) {
        productService.assignProductToMenu(id, menuId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a specific product by ID. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(
            summary = "Update product",
            description = "Update product information. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @Operation(
            summary = "Delete product",
            description = "Delete a product from the system. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Move product to another menu",
            description = "Move a product from its current menu to another menu. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product moved successfully",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product or menu not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/move-menu/{menuId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> moveProductToMenu(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Target menu ID") @PathVariable Long menuId) {
        return ResponseEntity.ok(productService.moveProductToMenu(id, menuId));
    }

    @Operation(
            summary = "Get product ingredients",
            description = "Retrieve all ingredients for a specific product. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product ingredients retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductIngredientDto>> getProductIngredients(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        return ResponseEntity.ok(productIngredientService.getIngredientsForProduct(id));
    }

    @Operation(
            summary = "Add ingredient to product",
            description = "Add a single ingredient to a product with specified quantity. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient added successfully",
                    content = @Content(schema = @Schema(implementation = ProductIngredientDto.class))),
            @ApiResponse(responseCode = "404", description = "Product or ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductIngredientDto> addIngredient(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestBody AddIngredientRequest request) {
        ProductIngredientDto result = productIngredientService.addIngredientToProduct(
                id, request.getIngredientId(), request.getQuantity());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Add multiple ingredients to product (batch)",
            description = "Add multiple ingredients to a product in a single request. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredients added successfully"),
            @ApiResponse(responseCode = "404", description = "Product or ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/ingredients/batch")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductIngredientDto>> addIngredientsBatch(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestBody BatchIngredientsRequest request) {
        List<ProductIngredientDto> result = productIngredientService.addIngredientsToProduct(
                id, request.getIngredients());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Update ingredient quantity",
            description = "Update the quantity of an ingredient in a product. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductIngredientDto.class))),
            @ApiResponse(responseCode = "404", description = "Product or ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/ingredients/{ingredientId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductIngredientDto> updateIngredientQuantity(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Ingredient ID") @PathVariable Long ingredientId,
            @RequestBody AddIngredientRequest request) {
        ProductIngredientDto result = productIngredientService.updateIngredientQuantity(
                id, ingredientId, request.getQuantity());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Remove ingredient from product",
            description = "Remove an ingredient from a product. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient removed successfully"),
            @ApiResponse(responseCode = "404", description = "Product or ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/ingredients/{ingredientId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeIngredient(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Ingredient ID") @PathVariable Long ingredientId) {
        productIngredientService.removeIngredientFromProduct(id, ingredientId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Replace all product ingredients",
            description = "Replace all ingredients for a product with new ones. Manager access only.",
            tags = {"Products"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredients replaced successfully"),
            @ApiResponse(responseCode = "404", description = "Product or ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProductIngredientDto>> replaceAllIngredients(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestBody BatchIngredientsRequest request) {
        List<ProductIngredientDto> result = productIngredientService.replaceAllIngredients(
                id, request.getIngredients());
        return ResponseEntity.ok(result);
    }
}
