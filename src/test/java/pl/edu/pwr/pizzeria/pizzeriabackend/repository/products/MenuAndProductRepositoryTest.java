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
@Transactional // Cleans up the database after every test method
class MenuAndProductRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ProductRepository productRepository;

    // --- SECTION 1: FUNCTIONAL TESTS (RELATIONSHIPS) ---

    @Test
    @DisplayName("Relationship: Should save Menu and link Products to it")
    void shouldSaveMenuAndProducts() {
        // 1. GIVEN: Create and save a Menu first
        Menu menu = new Menu();
        menu.setName("Winter Specials");
        menu.setDescription("Limited time offer");
        menu.setIsActive(true);

        Menu savedMenu = menuRepository.save(menu);
        assertThat(savedMenu.getId()).isNotNull();

        // 2. WHEN: Create a Product linked to this Menu
        Product product = Product.builder()
                .name("Spicy Salami")
                .description("Very hot")
                .basePrice(new BigDecimal("35.50"))
                .isAvailable(true)
                .imageUrl("http://img.com/pizza.png")
                .menu(savedMenu) // <--- LINKING HERE
                .build();

        Product savedProduct = productRepository.save(product);

        // 3. THEN: Verify the product is saved and linked correctly
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getMenu().getId()).isEqualTo(savedMenu.getId());
    }

    @Test
    @DisplayName("Custom Query: Should find all products by Menu ID")
    void shouldFindProductsByMenuId() {
        // 1. Create a Menu
        Menu menu = new Menu();
        menu.setName("Drinks");
        menu.setIsActive(true);
        Menu savedMenu = menuRepository.save(menu);

        // 2. Add 3 products to this menu
        productRepository.save(createProduct(savedMenu, "Cola"));
        productRepository.save(createProduct(savedMenu, "Fanta"));
        productRepository.save(createProduct(savedMenu, "Sprite"));

        // 3. Add 1 product to a DIFFERENT menu (noise data)
        Menu otherMenu = new Menu();
        otherMenu.setName("Other");
        menuRepository.save(otherMenu);
        productRepository.save(createProduct(otherMenu, "Tea"));

        // 4. Execute the custom repository method
        List<Product> drinks = productRepository.findByMenuId(savedMenu.getId());

        // 5. Verify results
        assertThat(drinks).hasSize(3);
        assertThat(drinks).extracting(Product::getName)
                .containsExactlyInAnyOrder("Cola", "Fanta", "Sprite");
    }

    // --- SECTION 2: PERFORMANCE TESTS ---

    @Test
    @DisplayName("Performance: Insert 1,000 Products for one Menu")
    void testBulkInsertProducts() {
        // 1. Prepare data
        Menu menu = new Menu();
        menu.setName("Mega Menu");
        menuRepository.save(menu);

        int count = 1000;
        List<Product> products = new ArrayList<>(count);

        // 2. Generate objects in memory
        for (int i = 0; i < count; i++) {
            products.add(Product.builder()
                    .name("Pizza #" + i)
                    .basePrice(BigDecimal.valueOf(20 + i))
                    .menu(menu)
                    .isAvailable(true)
                    .build());
        }

        // 3. Measure INSERT time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        productRepository.saveAll(products); // Batch save
        productRepository.flush();           // Force Hibernate to write to DB immediately

        stopWatch.stop();

        // 4. Log results
        System.out.println("--------------------------------------------------");
        System.out.println("Inserted " + count + " products in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("Average time per product: " + ((double) stopWatch.getTotalTimeMillis() / count) + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(productRepository.count()).isGreaterThanOrEqualTo(count);
    }

    @Test
    @DisplayName("Performance: Read/Select 1,000 Products")
    void testBulkReadProducts() {
        // 1. Setup: Insert 1000 items first
        Menu menu = new Menu();
        menu.setName("Read Test Menu");
        menuRepository.save(menu);

        List<Product> batch = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            batch.add(createProduct(menu, "Item " + i));
        }
        productRepository.saveAll(batch);
        productRepository.flush();

        // 2. Measure SELECT time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Fetching all products assigned to this menu
        List<Product> found = productRepository.findByMenuId(menu.getId());

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Selected " + found.size() + " products in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(found).hasSize(1000);
    }

    // --- HELPER METHODS ---

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