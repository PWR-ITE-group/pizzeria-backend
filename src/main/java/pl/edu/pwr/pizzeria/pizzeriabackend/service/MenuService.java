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

    @Transactional(readOnly = true)
    public List<MenuDto> getPublicMenu() {
        return menuRepository.findByIsActiveTrue().stream()
                .map(this::mapToMenuDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuDto> getAllMenusForManager() {
        return menuRepository.findAll().stream()
                .map(this::mapToMenuDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuDto getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu nie znalezione: " + id));
        return mapToMenuDto(menu);
    }

    @Transactional
    public MenuDto createMenu(MenuDto menuDto) {
        Menu menu = new Menu();
        menu.setName(menuDto.getName());
        menu.setDescription(menuDto.getDescription());
        menu.setIsActive(true);

        Menu savedMenu = menuRepository.save(menu);
        return mapToMenuDto(savedMenu);
    }

    @Transactional
    public MenuDto updateMenu(Long id, MenuDto menuDto) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu nie znalezione: " + id));

        menu.setName(menuDto.getName());
        menu.setDescription(menuDto.getDescription());

        if (menu.getIsActive() != menuDto.isActive()) {
            menu.setIsActive(menuDto.isActive());
        }

        Menu savedMenu = menuRepository.save(menu);
        return mapToMenuDto(savedMenu);
    }

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
        product.setIsAvailable(true);

        Product savedProduct = productRepository.save(product);
        return mapToProductDto(savedProduct);
    }

    @Transactional
    public void updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony"));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setImageUrl(dto.getImageUrl());

        productRepository.save(product);
    }

    @Transactional
    public void changeProductAvailability(Long id, boolean isAvailable) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony"));
        product.setIsAvailable(isAvailable);
        productRepository.save(product);
    }

    @Transactional
    public void detachProductFromMenu(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony"));

        product.setMenu(null);
        product.setIsAvailable(false);

        productRepository.save(product);
    }

    @Transactional
    public ProductDto addProductToMenu(Long menuId, Long productId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu nie znalezione: " + menuId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produkt nie znaleziony: " + productId));

        product.setMenu(menu);
        product.setIsAvailable(true);

        Product savedProduct = productRepository.save(product);
        return mapToProductDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produkt nie znaleziony");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu nie znalezione: " + id));

        List<Product> products = productRepository.findAllByMenuId(id);
        if (!products.isEmpty()) {
            throw new RuntimeException("Nie można usunąć menu, które zawiera produkty. " +
                    "Najpierw usuń lub przenieś produkty z tego menu.");
        }

        menuRepository.deleteById(id);
    }


    private MenuDto mapToMenuDto(Menu menu) {
        List<ProductDto> products = productRepository.findAllByMenuId(menu.getId()).stream()
                .map(this::mapToProductDto)
                .collect(Collectors.toList());

        return MenuDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .active(menu.getIsActive())
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
