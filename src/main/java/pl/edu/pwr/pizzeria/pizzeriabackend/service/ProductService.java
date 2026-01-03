package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.MenuRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MenuRepository menuRepository;
    private final ProductIngredientService productIngredientService;

    public ProductService(ProductRepository productRepository, MenuRepository menuRepository, ProductIngredientService productIngredientService) {
        this.productRepository = productRepository;
        this.menuRepository = menuRepository;
        this.productIngredientService = productIngredientService;
    }

    /**
     * Создать продукт в "Каталоге" (без привязки к меню).
     * Он будет скрыт (isAvailable = false), пока его не добавят в меню.
     */
    @Transactional
    public ProductDto createProductInCatalog(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setImageUrl(dto.getImageUrl());

        // Важно: явно указываем, что меню нет
        product.setMenu(null);
        product.setIsAvailable(false);

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    /**
     * Назначить существующий продукт в меню.
     * Продукт переносится в новое меню и становится доступным.
     */
    @Transactional
    public void assignProductToMenu(Long productId, Long menuId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + menuId));

        product.setMenu(menu);
        product.setIsAvailable(true); // Активируем при добавлении

        productRepository.save(product);
    }

    /**
     * Получить список "сирот" (продуктов без меню).
     * Нужно, чтобы менеджер мог выбрать из них.
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsWithoutMenu() {
        // Требуется добавить метод findByMenuIsNull() в репозиторий
        return productRepository.findByMenuIsNull().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Обновить данные продукта (цена, название).
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setImageUrl(dto.getImageUrl());

        // isAvailable обновляем отдельным методом или оставляем как есть
        if (dto.isAvailable() != product.getIsAvailable()) {
            product.setIsAvailable(dto.isAvailable());
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    /**
     * Получить продукт по ID.
     */
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToDtoWithMenuInfo(product);
    }

    /**
     * Удалить продукт (жесткое удаление).
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    /**
     * Переместить продукт из одного меню в другое.
     */
    @Transactional
    public ProductDto moveProductToMenu(Long productId, Long menuId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + menuId));

        product.setMenu(menu);
        // Продукт остается доступным при перемещении
        Product saved = productRepository.save(product);
        return mapToDtoWithMenuInfo(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProductsWithStatus() {
        return productRepository.findAll().stream()
                .map(this::mapToDtoWithMenuInfo)
                .collect(Collectors.toList());
    }

    private ProductDto mapToDtoWithMenuInfo(Product product) {
        ProductDto.ProductDtoBuilder builder = ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .imageUrl(product.getImageUrl())
                .available(product.getIsAvailable() != null && product.getIsAvailable());

        // Jeśli produkt jest przypisany do menu, dodajemy info
        if (product.getMenu() != null) {
            builder.menuId(product.getMenu().getId());
            builder.menuName(product.getMenu().getName());
        }

        // Populate ingredients
        builder.ingredients(productIngredientService.getIngredientsForProduct(product.getId()));

        return builder.build();
    }

    // --- Mapper ---
    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .imageUrl(product.getImageUrl())
                .available(product.getIsAvailable() != null && product.getIsAvailable())
                .ingredients(productIngredientService.getIngredientsForProduct(product.getId()))
                .build();
    }
}
