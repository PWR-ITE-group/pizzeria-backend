package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AddItemRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ApplyPromotionRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateClientOrderRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateCustomPizzaRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateOrderRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ModifyPizzaRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PaymentDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PromotionDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateItemQuantityRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateItemStatusRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateOrderStatusRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.OrderService;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.PaymentService;

import java.util.List;

@Tag(name = "Orders", description = "Order management endpoints for creating, updating, and managing orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final EmployeeRepository employeeRepository;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, EmployeeRepository employeeRepository, PaymentService paymentService) {
        this.orderService = orderService;
        this.employeeRepository = employeeRepository;
        this.paymentService = paymentService;
    }

    // ===== PHASE 1: ULTRA-SIMPLE ORDERS =====

    // 1. Create empty order
    @Operation(
            summary = "Create order",
            description = "Create a new empty order. The order type (DINE_IN, TAKEAWAY, DELIVERY) must be specified.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
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

    // 2. Get all orders (with optional status filter)
    @Operation(
            summary = "Get all orders",
            description = "Retrieve all orders. Optionally filter by status (PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED).",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF')")
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @Parameter(description = "Optional order status filter") @RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(orderService.getOrdersByStatus(orderStatus));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid order status: " + status);
            }
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // 3. Get single order by ID
    @Operation(
            summary = "Get order by ID",
            description = "Retrieve a single order by its ID with all details including items, promotions, and payment status.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF')")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ===== PHASE 2: ORDER ITEMS =====

    // 4. Add item to order
    @Operation(
            summary = "Add item to order",
            description = "Add a product item to an existing order. The order total price will be recalculated automatically.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order or product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> addItemToOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestBody AddItemRequest request) {
        OrderDto result = orderService.addItemToOrder(
                id,
                request.getProductId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(result);
    }

    // 5. Remove item from order
    @Operation(
            summary = "Remove item from order",
            description = "Remove a product item from an order. The order total price will be recalculated automatically.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order or item not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> removeItemFromOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Order item ID") @PathVariable Long itemId) {
        OrderDto result = orderService.removeItemFromOrder(id, itemId);
        return ResponseEntity.ok(result);
    }

    // 6. Update item quantity
    @Operation(
            summary = "Update item quantity",
            description = "Update the quantity of a product item in an order. The order total price will be recalculated automatically.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "404", description = "Order or item not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> updateItemQuantity(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Order item ID") @PathVariable Long itemId,
            @RequestBody UpdateItemQuantityRequest request) {
        OrderDto result = orderService.updateItemQuantity(id, itemId, request.getQuantity());
        return ResponseEntity.ok(result);
    }

    // ===== PHASE 3: STATUS WORKFLOW =====

    // 7. Update order status
    @Operation(
            summary = "Update order status",
            description = "Update the status of an order. Valid statuses: PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or status transition"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF', 'COURIER')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        OrderDto result = orderService.updateOrderStatus(id, request.getStatus(), authentication);
        return ResponseEntity.ok(result);
    }

    // 8. Update item status (CRITICAL: This triggers stock deduction!)
    @Operation(
            summary = "Update item status",
            description = "Update the status of an order item. CRITICAL: Changing status to 'ready' triggers automatic stock deduction. " +
                    "Valid statuses: PENDING, PREPARING, READY.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Order or item not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/items/{itemId}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<OrderDto> updateItemStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Order item ID") @PathVariable Long itemId,
            @RequestBody UpdateItemStatusRequest request) {
        OrderDto result = orderService.updateOrderItemStatus(id, itemId, request.getStatus());
        return ResponseEntity.ok(result);
    }

    // 9. Get orders by status
    @Operation(
            summary = "Get orders by status",
            description = "Retrieve all orders filtered by status. Valid statuses: PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'CHEF', 'COURIER')")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderDto> result = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }

    // Public API: Get order status by ID (no authentication required)
    @Operation(
            summary = "Get order status (public)",
            description = "Public endpoint to retrieve order status by ID. No authentication required. " +
                    "Useful for customers to check their order status.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/public/{id}/status")
    public ResponseEntity<OrderDto> getOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        // Return only basic info for public access
        return ResponseEntity.ok(order);
    }

    // ===== CLIENT ORDER CREATION (PUBLIC API) =====

    // Create order from client (public, no authentication)
    @Operation(
            summary = "Create client order (public)",
            description = "Public endpoint for clients to create orders. No authentication required. " +
                    "Returns order with tracking token and URL for order status tracking.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/public")
    public ResponseEntity<OrderDto> createClientOrder(
            @RequestBody CreateClientOrderRequest request) {
        OrderDto result = orderService.createClientOrder(request);
        return ResponseEntity.ok(result);
    }

    // Get order by tracking token (public, no authentication)
    @Operation(
            summary = "Track order by token (public)",
            description = "Public endpoint to retrieve order status by tracking token. No authentication required. " +
                    "Useful for customers to track their order using the tracking link provided when order was created.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found with provided tracking token")
    })
    @GetMapping("/track/{token}")
    public ResponseEntity<OrderDto> trackOrder(
            @Parameter(description = "Tracking token") @PathVariable String token) {
        OrderDto order = orderService.getOrderByTrackingToken(token);
        return ResponseEntity.ok(order);
    }

    // Get order by ID with tracking token (public, alternative method)
    @Operation(
            summary = "Get order tracking info by ID (public)",
            description = "Public endpoint to retrieve order tracking information by ID. " +
                    "Returns order with tracking token for customers who lost their tracking link.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order tracking info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or has no tracking token")
    })
    @GetMapping("/public/{id}/track")
    public ResponseEntity<OrderDto> getOrderTrackingInfo(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        OrderDto orderDto = orderService.getOrderById(id);
        if (orderDto.getTrackingToken() == null) {
            throw new RuntimeException("Order does not have a tracking token");
        }
        // Return the order with tracking info
        return ResponseEntity.ok(orderDto);
    }

    // ===== PIZZA CONFIGURATION ENDPOINTS =====

    // Add custom pizza to order
    @Operation(
            summary = "Add custom pizza to order",
            description = "Create a custom pizza from scratch with selected ingredients and add it to the order.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Custom pizza added successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/items/custom")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> addCustomPizza(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestBody CreateCustomPizzaRequest request) {
        OrderDto result = orderService.addCustomPizzaToOrder(id, request);
        return ResponseEntity.ok(result);
    }

    // Add modified pizza to order
    @Operation(
            summary = "Add modified pizza to order",
            description = "Add a pizza based on an existing product with added/removed ingredients.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modified pizza added successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order or product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/items/modified")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> addModifiedPizza(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestBody ModifyPizzaRequest request) {
        OrderDto result = orderService.addModifiedPizzaToOrder(id, request);
        return ResponseEntity.ok(result);
    }

    // Get payment status for an order
    @Operation(
            summary = "Get payment status",
            description = "Retrieve payment information for an order, including payment status, method, and amount.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404", description = "Order or payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/payment-status")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> getPaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        try {
            PaymentDto payment = paymentService.getPaymentByOrderId(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            // If payment doesn't exist, return 404
            return ResponseEntity.notFound().build();
        }
    }

    // ===== PROMOTION OPERATIONS =====

    // Apply promotion to order
    @Operation(
            summary = "Apply promotion to order",
            description = "Apply a promotion code to an order. The order total price will be recalculated with the discount applied.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion applied successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid promotion code or promotion not applicable"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/promotions")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> applyPromotion(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestBody ApplyPromotionRequest request) {
        return ResponseEntity.ok(orderService.applyPromotion(id, request.getCode()));
    }

    // Remove promotion from order
    @Operation(
            summary = "Remove promotion from order",
            description = "Remove a promotion from an order. The order total price will be recalculated without the discount.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion removed successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order or promotion not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/promotions/{promotionId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<OrderDto> removePromotion(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Promotion ID") @PathVariable Long promotionId) {
        return ResponseEntity.ok(orderService.removePromotion(id, promotionId));
    }

    // Get promotions applied to order
    @Operation(
            summary = "Get order promotions",
            description = "Retrieve all promotions currently applied to an order.",
            tags = {"Orders"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/promotions")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<List<PromotionDto>> getOrderPromotions(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderPromotions(id));
    }

    // Helper method to extract employee ID from authentication
    private Long getEmployeeIdFromAuth(Authentication authentication) {
        String login = authentication.getName();
        return employeeRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Employee not found"))
                .getId();
    }
}

