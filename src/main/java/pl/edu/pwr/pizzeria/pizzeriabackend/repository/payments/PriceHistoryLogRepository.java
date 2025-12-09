package pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.PriceHistoryLog;

import java.util.List;

@Repository
public interface PriceHistoryLogRepository extends JpaRepository<PriceHistoryLog, Long> {
    // Найти историю цен конкретного продукта
    List<PriceHistoryLog> findByProductIdOrderByChangedAtDesc(Long productId);
}
