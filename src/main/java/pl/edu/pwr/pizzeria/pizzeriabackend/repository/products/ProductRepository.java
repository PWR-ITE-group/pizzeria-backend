package pl.edu.pwr.pizzeria.pizzeriabackend.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByMenuId(Long menuId);

    List<Product> findByMenuIdAndIsAvailableTrue(Long menuId);

    List<Product> findByMenuIsNull();
}
