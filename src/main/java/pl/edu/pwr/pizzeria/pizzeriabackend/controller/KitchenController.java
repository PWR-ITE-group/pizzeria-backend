package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen")
@PreAuthorize("hasRole('CHEF')")
public class KitchenController {

    // @Autowired KitchenService kitchenService;

    @GetMapping("/queue")
    public ResponseEntity<Object> getPendingOrderItems() {
        // TODO: Wywołanie kitchenService.getPreparationQueue()
        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/items/{itemId}/start")
    public ResponseEntity<Void> startPreparation(@PathVariable Long itemId) {
        // TODO: Wywołanie kitchenService.updateItemStatus(itemId, "PREPARING")
        // TODO: W tym miejscu powinna zostać uruchomiona logika zużycia składników (inventoryService)
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{itemId}/finish")
    public ResponseEntity<Void> finishPreparation(@PathVariable Long itemId) {
        // TODO: Wywołanie kitchenService.updateItemStatus(itemId, "READY")
        return ResponseEntity.ok().build();
    }
}