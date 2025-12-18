package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.MenuDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.MenuRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final ProductRepository productRepository;

    public MenuService(MenuRepository menuRepository, ProductRepository productRepository) {
        this.menuRepository = menuRepository;
        this.productRepository = productRepository;
    }

    // UC1: Pobieranie menu dla klientów (Tylko aktywne)
    @Transactional(readOnly = true)
    public List<MenuDto> getPublicMenu() {
        return menuRepository.findByIsActiveTrue().stream()
                .map(this::mapToMenuDto)
                .collect(Collectors.toList());
    }

    // UC17, UC13: Pobieranie wszystkich menu dla managera (w tym ukryte)
    @Transactional(readOnly = true)
    public List<MenuDto> getAllMenusForManager() {
        return menuRepository.findAll().stream()
                .map(this::mapToMenuDto)
                .collect(Collectors.toList());
    }

    // UC17: Utworzenie nowej karty menu
    @Transactional
    public MenuDto createMenu(MenuDto menuDto) {
        Menu menu = new Menu();
        menu.setName(menuDto.getName());
        menu.setDescription(menuDto.getDescription()); // Zapisujemy opis
        menu.setIsActive(true); // Domyślnie aktywne

        Menu savedMenu = menuRepository.save(menu);
        return mapToMenuDto(savedMenu);
    }

    // UC17 (Edit): Edycja menu (zmiana nazwy, opisu, statusu)
    @Transactional
    public MenuDto updateMenu(Long id, MenuDto menuDto) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu nie znalezione: " + id));

        menu.setName(menuDto.getName());
        menu.setDescription(menuDto.getDescription());

        // WAŻNE: Zmiana statusu na false uruchomi trigger 'trg_cascade_menu_disable' w bazie danych!
        // To automatycznie ukryje wszystkie produkty z tego menu (is_available -> false).
        if (menu.getIsActive() != menuDto.isActive()) {
            menu.setIsActive(menuDto.isActive());
        }

        Menu savedMenu = menuRepository.save(menu);
        return mapToMenuDto(savedMenu);
    }

    // UC13: Dodanie nowego produktu bezpośrednio do menu
    @Transactional
    public ProductDto addProduct(Long menuId, ProductDto productDto) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu nie znalezione"));

        Product product = new Product();
        product.setMenu(menu);
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setBasePrice(productDto.getBasePrice());
        product.setImageUrl(productDto.getImageUrl());
        product.setIsAvailable(true); // Domyślnie dostępny

        Product savedProduct = productRepository.save(product);
        return mapToProductDto(savedProduct);
    }

    // UC18: Aktualizacja ceny lub opisu produktu
    @Transactional
    public void updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony"));

        // Aktualizacja pól
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setImageUrl(dto.getImageUrl());

        productRepository.save(product);
        // Trigger 'trg_audit_product_price' w bazie danych zapisze historię zmian ceny
    }

    // UC19: Zmiana dostępności produktu (dostępny/niedostępny)
    @Transactional
    public void changeProductAvailability(Long id, boolean isAvailable) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony"));
        product.setIsAvailable(isAvailable);
        productRepository.save(product);
    }

    // UC14: Usuwanie produktu
    @Transactional
    public void deleteProduct(Long id) {
        // Ważna uwaga z raportu:
        // Jeśli produkt był już w zamówieniach, baza danych może zablokować usunięcie (FK constraint).
        // W takim przypadku Manager powinien go tylko "ukryć" (changeProductAvailability -> false).
        // Tutaj realizujemy twarde usuwanie (Hard Delete) dla pomyłkowo dodanych produktów.
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produkt nie znaleziony");
        }
        productRepository.deleteById(id);
    }

    // --- Mappery (Konwertery Entity -> DTO) ---

    private MenuDto mapToMenuDto(Menu menu) {
        // Pobieramy produkty przypisane do tego menu
        List<ProductDto> products = productRepository.findAllByMenuId(menu.getId()).stream()
                .map(this::mapToProductDto)
                .collect(Collectors.toList());

        return MenuDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription()) // <-- Dodano obsługę opisu
                .active(menu.getIsActive())         // <-- Dodano obsługę statusu
                .products(products)
                .build();
    }

    private ProductDto mapToProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .imageUrl(product.getImageUrl())
                .available(product.getIsAvailable())
                .build();
    }
}
