package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

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
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.MenuDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.MenuService;

import java.util.List;

@Tag(name = "Menus", description = "Menu management endpoints for managing menus and products within menus")
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // UC1: Dostęp publiczny (widoczny dla wszystkich klientów)
    @Operation(
            summary = "Get public menu",
            description = "Retrieve all active menus visible to customers. No authentication required.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public menu retrieved successfully")
    })
    @GetMapping("/public")
    public ResponseEntity<List<MenuDto>> getPublicMenu() {
        return ResponseEntity.ok(menuService.getPublicMenu());
    }

    // Tylko MANAGER widzi wszystko (w tym ukryte i nieaktywne menu)
    @Operation(
            summary = "Get all menus (manager)",
            description = "Retrieve all menus including hidden and inactive ones. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menus retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/manage")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<MenuDto>> getManagerMenu() {
        return ResponseEntity.ok(menuService.getAllMenusForManager());
    }

    // Get menu by ID
    @Operation(
            summary = "Get menu by ID",
            description = "Retrieve a specific menu by ID. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MenuDto.class))),
            @ApiResponse(responseCode = "404", description = "Menu not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MenuDto> getMenuById(
            @Parameter(description = "Menu ID") @PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    // UC17: Utworzenie nowej karty menu
    @Operation(
            summary = "Create menu",
            description = "Create a new menu card. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu created successfully",
                    content = @Content(schema = @Schema(implementation = MenuDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(menuService.createMenu(menuDto));
    }

    // UC17 (Edit): Aktualizacja menu (nazwa, opis, status aktywności)
    @Operation(
            summary = "Update menu",
            description = "Update menu information (name, description, active status). Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu updated successfully",
                    content = @Content(schema = @Schema(implementation = MenuDto.class))),
            @ApiResponse(responseCode = "404", description = "Menu not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MenuDto> updateMenu(
            @Parameter(description = "Menu ID") @PathVariable Long id,
            @RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(menuService.updateMenu(id, menuDto));
    }

    // UC13: Dodanie produktu do konkretnego menu
    // URL: /api/menu/1/products
    @Operation(
            summary = "Add product to menu",
            description = "Create a new product and add it to a specific menu. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product created and added to menu",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Menu not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{menuId}/products")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> addProduct(
            @Parameter(description = "Menu ID") @PathVariable Long menuId,
            @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(menuService.addProduct(menuId, productDto));
    }

    // UC18: Manager edytuje produkt (cena, opis, zdjęcie)
    @Operation(
            summary = "Update product",
            description = "Update product information (price, description, image). Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestBody ProductDto productDto) {
        menuService.updateProduct(id, productDto);
        return ResponseEntity.ok().build();
    }

    // Add existing product by ID to menu
    @Operation(
            summary = "Assign existing product to menu",
            description = "Assign an existing product to a menu. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product assigned to menu",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Menu or product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{menuId}/products/{productId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> addProductToMenu(
            @Parameter(description = "Menu ID") @PathVariable Long menuId,
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        return ResponseEntity.ok(menuService.addProductToMenu(menuId, productId));
    }

    // UC19: Zmiana dostępności produktu (dostępny/niedostępny)
    @Operation(
            summary = "Change product availability",
            description = "Update product availability status (available/unavailable). Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product availability updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/products/{id}/availability")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> changeAvailability(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Availability status") @RequestParam boolean available) {
        menuService.changeProductAvailability(id, available);
        return ResponseEntity.ok().build();
    }

    // UC14: Usuwanie produktu
    @Operation(
            summary = "Delete product",
            description = "Delete a product from the system. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        menuService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Detach product from menu",
            description = "Remove product from its menu (product remains in catalog). Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product detached from menu successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/products/{id}/detach")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> detachProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        menuService.detachProductFromMenu(id);
        return ResponseEntity.ok().build();
    }

    // Delete menu
    @Operation(
            summary = "Delete menu",
            description = "Delete a menu from the system. Manager access only.",
            tags = {"Menus"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Menu not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteMenu(
            @Parameter(description = "Menu ID") @PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }
}
