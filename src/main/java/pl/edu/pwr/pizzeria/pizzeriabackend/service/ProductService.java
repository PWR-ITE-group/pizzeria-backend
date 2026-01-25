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

        product.setMenu(null);
        product.setIsAvailable(false);

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }


    @Transactional
    public void assignProductToMenu(Long productId, Long menuId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + menuId));

        product.setMenu(menu);
        product.setIsAvailable(true);

        productRepository.save(product);
    }


    @Transactional(readOnly = true)
    public List<ProductDto> getProductsWithoutMenu() {
        return productRepository.findByMenuIsNull().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setImageUrl(dto.getImageUrl());

        if (dto.isAvailable() != product.getIsAvailable()) {
            product.setIsAvailable(dto.isAvailable());
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToDtoWithMenuInfo(product);
    }


    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductDto moveProductToMenu(Long productId, Long menuId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + menuId));

        product.setMenu(menu);
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

        if (product.getMenu() != null) {
            builder.menuId(product.getMenu().getId());
            builder.menuName(product.getMenu().getName());
        }

        builder.ingredients(productIngredientService.getIngredientsForProduct(product.getId()));

        return builder.build();
    }

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
