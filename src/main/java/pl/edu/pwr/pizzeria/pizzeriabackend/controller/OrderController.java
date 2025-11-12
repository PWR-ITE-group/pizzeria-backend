package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderRequestDTO;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderStatusDTO;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    // @Autowired OrderService orderService;

    // --- Publiczny endpoint dla klienta ---
    @PostMapping("/orders")
    public ResponseEntity<OrderStatusDTO> placeOrder(@RequestBody OrderRequestDTO request) {
        // TODO: Wywołanie orderService.placeOrder(request)
        return ResponseEntity.ok(new OrderStatusDTO());
    }

    @GetMapping("/orders/{token}/status")
    public ResponseEntity<OrderStatusDTO> getOrderStatus(@PathVariable String token) {
        // TODO: Wywołanie orderService.getOrderStatusByToken(token)
        return ResponseEntity.ok(new OrderStatusDTO());
    }

    // --- Pracowniczy endpoint (WAITER) ---
    @PostMapping("/waiter/orders/dine_in")
    @PreAuthorize("hasRole('WAITER')")
    public ResponseEntity<OrderStatusDTO> createDineInOrder(@RequestBody OrderRequestDTO request) {
        // TODO: Wywołanie orderService.createDineInOrder(request)
        return ResponseEntity.ok(new OrderStatusDTO());
    }
}