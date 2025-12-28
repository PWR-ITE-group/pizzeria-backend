package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AddItemRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateOrderRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateItemQuantityRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateItemStatusRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateOrderStatusRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final EmployeeRepository employeeRepository;

    public OrderController(OrderService orderService, EmployeeRepository employeeRepository) {
        this.orderService = orderService;
        this.employeeRepository = employeeRepository;
    }

    // ===== PHASE 1: ULTRA-SIMPLE ORDERS =====

    // 1. Create empty order
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        // Automatically get employee ID from authenticated user
        Long employeeId = getEmployeeIdFromAuth(authentication);
        
        OrderDto result = orderService.createOrder(request.getOrderType(), employeeId);
        return ResponseEntity.ok(result);
    }

    // 2. Get all orders
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // 3. Get single order by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ===== PHASE 2: ORDER ITEMS =====

    // 4. Add item to order
    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> addItemToOrder(
            @PathVariable Long id,
            @RequestBody AddItemRequest request) {
        OrderDto result = orderService.addItemToOrder(
                id,
                request.getProductId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(result);
    }

    // 5. Remove item from order
    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> removeItemFromOrder(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        OrderDto result = orderService.removeItemFromOrder(id, itemId);
        return ResponseEntity.ok(result);
    }

    // 6. Update item quantity
    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> updateItemQuantity(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody UpdateItemQuantityRequest request) {
        OrderDto result = orderService.updateItemQuantity(id, itemId, request.getQuantity());
        return ResponseEntity.ok(result);
    }

    // ===== PHASE 3: STATUS WORKFLOW =====

    // 7. Update order status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF', 'COURIER')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        OrderDto result = orderService.updateOrderStatus(id, request.getStatus(), authentication);
        return ResponseEntity.ok(result);
    }

    // 8. Update item status (CRITICAL: This triggers stock deduction!)
    @PutMapping("/{id}/items/{itemId}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<OrderDto> updateItemStatus(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody UpdateItemStatusRequest request) {
        OrderDto result = orderService.updateOrderItemStatus(id, itemId, request.getStatus());
        return ResponseEntity.ok(result);
    }

    // 9. Get orders by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF', 'COURIER')")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderDto> result = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }

    // Helper method to extract employee ID from authentication
    private Long getEmployeeIdFromAuth(Authentication authentication) {
        String login = authentication.getName();
        return employeeRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Employee not found"))
                .getId();
    }
}

