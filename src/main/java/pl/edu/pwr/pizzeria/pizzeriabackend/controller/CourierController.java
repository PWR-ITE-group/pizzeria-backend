package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courier")
@PreAuthorize("hasRole('COURIER')")
public class CourierController {

    // @Autowired DeliveryService deliveryService;

    @GetMapping("/deliveries/assigned")
    public ResponseEntity<Object> getAssignedDeliveries() {
        // TODO: Wywołanie deliveryService.getDeliveriesForCourier(currentUserId)
        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/deliveries/{id}/depart")
    public ResponseEntity<Void> setInTransit(@PathVariable Long id) {
        // TODO: Wywołanie deliveryService.updateDeliveryStatus(id, "IN_TRANSIT")
        // TODO: Zapis czasu assigned_at
        return ResponseEntity.ok().build();
    }

    @PutMapping("/deliveries/{id}/complete")
    public ResponseEntity<Void> setDelivered(@PathVariable Long id) {
        // TODO: Wywołanie deliveryService.updateDeliveryStatus(id, "DELIVERED")
        // TODO: Zapis czasu delivered_at
        return ResponseEntity.ok().build();
    }
}