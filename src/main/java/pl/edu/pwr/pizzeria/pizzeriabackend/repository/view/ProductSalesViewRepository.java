package pl.edu.pwr.pizzeria.pizzeriabackend.repository.view;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.ProductSalesView;

public interface ProductSalesViewRepository extends JpaRepository<ProductSalesView, Long> {}
