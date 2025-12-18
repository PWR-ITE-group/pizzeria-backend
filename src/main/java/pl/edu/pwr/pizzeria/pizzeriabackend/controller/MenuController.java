package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.MenuDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // UC1: Dostęp publiczny (widoczny dla wszystkich klientów)
    @GetMapping("/public")
    public ResponseEntity<List<MenuDto>> getPublicMenu() {
        return ResponseEntity.ok(menuService.getPublicMenu());
    }

    // Tylko MANAGER widzi wszystko (w tym ukryte i nieaktywne menu)
    @GetMapping("/manage")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<MenuDto>> getManagerMenu() {
        return ResponseEntity.ok(menuService.getAllMenusForManager());
    }

    // UC17: Utworzenie nowej karty menu
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(menuService.createMenu(menuDto));
    }

    // UC17 (Edit): Aktualizacja menu (nazwa, opis, status aktywności)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable Long id, @RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(menuService.updateMenu(id, menuDto));
    }

    // UC13: Dodanie produktu do konkretnego menu
    // URL: /api/menu/1/products
    @PostMapping("/{menuId}/products")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> addProduct(@PathVariable Long menuId, @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(menuService.addProduct(menuId, productDto));
    }

    // UC18: Manager edytuje produkt (cena, opis, zdjęcie)
    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        menuService.updateProduct(id, productDto);
        return ResponseEntity.ok().build();
    }

    // UC19: Zmiana dostępności produktu (dostępny/niedostępny)
    @PatchMapping("/products/{id}/availability")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> changeAvailability(@PathVariable Long id, @RequestParam boolean available) {
        menuService.changeProductAvailability(id, available);
        return ResponseEntity.ok().build();
    }

    // UC14: Usuwanie produktu
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        menuService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
