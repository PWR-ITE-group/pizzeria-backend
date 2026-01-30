package pl.edu.pwr.pizzeria.pizzeriabackend.repository.products;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MenuAndProductRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ProductRepository productRepository;


    @Test
    @DisplayName("Relationship: Should save Menu and link Products to it")
    void shouldSaveMenuAndProducts() {
        Menu menu = new Menu();
        menu.setName("Winter Specials");
        menu.setDescription("Limited time offer");
        menu.setIsActive(true);

        Menu savedMenu = menuRepository.save(menu);
        assertThat(savedMenu.getId()).isNotNull();

        Product product = Product.builder()
                .name("Spicy Salami")
                .description("Very hot")
                .basePrice(new BigDecimal("35.50"))
                .isAvailable(true)
                .imageUrl("http://img.com/pizza.png")
                .menu(savedMenu) // <--- LINKING HERE
                .build();

        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getMenu().getId()).isEqualTo(savedMenu.getId());
    }

    @Test
    @DisplayName("Custom Query: Should find all products by Menu ID")
    void shouldFindProductsByMenuId() {
        Menu menu = new Menu();
        menu.setName("Drinks");
        menu.setIsActive(true);
        Menu savedMenu = menuRepository.save(menu);

        productRepository.save(createProduct(savedMenu, "Cola"));
        productRepository.save(createProduct(savedMenu, "Fanta"));
        productRepository.save(createProduct(savedMenu, "Sprite"));

        Menu otherMenu = new Menu();
        otherMenu.setName("Other");
        menuRepository.save(otherMenu);
        productRepository.save(createProduct(otherMenu, "Tea"));

        List<Product> drinks = productRepository.findAllByMenuId(savedMenu.getId());

        assertThat(drinks).hasSize(3);
        assertThat(drinks).extracting(Product::getName)
                .containsExactlyInAnyOrder("Cola", "Fanta", "Sprite");
    }


    @Test
    @DisplayName("Performance: Insert 1,000 Products for one Menu")
    void testBulkInsertProducts() {
        Menu menu = new Menu();
        menu.setName("Mega Menu");
        menuRepository.save(menu);

        int count = 1000;
        List<Product> products = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            products.add(Product.builder()
                    .name("Pizza #" + i)
                    .basePrice(BigDecimal.valueOf(20 + i))
                    .menu(menu)
                    .isAvailable(true)
                    .build());
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        productRepository.saveAll(products);
        productRepository.flush();

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Inserted " + count + " products in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("Average time per product: " + ((double) stopWatch.getTotalTimeMillis() / count) + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(productRepository.count()).isGreaterThanOrEqualTo(count);
    }

    @Test
    @DisplayName("Performance: Read/Select 1,000 Products")
    void testBulkReadProducts() {
        Menu menu = new Menu();
        menu.setName("Read Test Menu");
        menuRepository.save(menu);

        List<Product> batch = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            batch.add(createProduct(menu, "Item " + i));
        }
        productRepository.saveAll(batch);
        productRepository.flush();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Product> found = productRepository.findAllByMenuId(menu.getId());

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Selected " + found.size() + " products in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(found).hasSize(1000);
    }


    private Product createProduct(Menu menu, String name) {
        return Product.builder()
                .name(name)
                .description("Description for " + name)
                .basePrice(new BigDecimal("10.00"))
                .isAvailable(true)
                .menu(menu)
                .build();
    }
}