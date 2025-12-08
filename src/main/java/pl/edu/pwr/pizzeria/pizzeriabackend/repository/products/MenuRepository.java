package pl.edu.pwr.pizzeria.pizzeriabackend.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Menu;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    // Найти только активные меню (для клиента)
    List<Menu> findByIsActiveTrue();
}

