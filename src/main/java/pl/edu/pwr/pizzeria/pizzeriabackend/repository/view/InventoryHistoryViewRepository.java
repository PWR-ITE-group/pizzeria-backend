package pl.edu.pwr.pizzeria.pizzeriabackend.repository.view;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.view.InventoryHistoryView;

public interface InventoryHistoryViewRepository extends JpaRepository<InventoryHistoryView,Integer> {
}
