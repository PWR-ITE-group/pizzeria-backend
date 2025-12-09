package pl.edu.pwr.pizzeria.pizzeriabackend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.edu.pwr.pizzeria.pizzeriabackend.AbstractIntegrationTest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.Menu;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MenuRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MenuRepository menuRepository;

    @Test
    void shouldSaveAndFindMenu() {
        Menu menu = new Menu();
        menu.setName("Test Docker Menu");
        menu.setIsActive(true);

        Menu saved = menuRepository.save(menu);

        assertThat(saved.getId()).isNotNull();
        Menu found = menuRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Test Docker Menu");
    }
}