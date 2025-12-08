package pl.edu.pwr.pizzeria.pizzeriabackend.repository.view;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.IngredientStockView;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.OrderFullInfo;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.ProductSalesView;

public interface OrderFullInfoRepository extends JpaRepository<OrderFullInfo, Long> {}

