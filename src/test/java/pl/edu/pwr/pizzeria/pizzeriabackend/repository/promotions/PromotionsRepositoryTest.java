package pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.Promotions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PromotionsRepositoryTest {

    @Autowired
    private PromotionsRepository promotionsRepository;

    @Test
    @DisplayName("Save & Find: Should save promotion and find it by ID")
    void shouldSaveAndFindPromotion() {
        // 1. Arrange
        Promotions promo = Promotions.builder()
                .code("SUMMER2024")
                .description("Summer Sale")
                .discount_percent(new BigDecimal("15.00"))
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(30))
                .isActive(true)
                .build();

        // 2. Act
        Promotions saved = promotionsRepository.save(promo);

        // 3. Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("SUMMER2024");
    }

    @Test
    @DisplayName("Validation: Should find promotion by unique CODE")
    void shouldFindByCode() {
        // Arrange
        Promotions p1 = createPromo("WELCOME10", true);
        Promotions p2 = createPromo("PIZZA50", false);
        promotionsRepository.save(p1);
        promotionsRepository.save(p2);

        Optional<Promotions> found = promotionsRepository.findByCode("WELCOME10");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(p1.getId());
        assertThat(found.get().getIsActive()).isTrue();
    }

    // Хелпер
    private Promotions createPromo(String code, boolean active) {
        return Promotions.builder()
                .code(code)
                .discount_percent(BigDecimal.TEN)
                .isActive(active)
                .build();
    }
}