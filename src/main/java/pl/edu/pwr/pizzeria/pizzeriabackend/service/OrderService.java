package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.Ingredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.ingredients.ProductIngredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItem;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItemIngredient;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.ModificationType;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.IngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.ingredients.ProductIngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderItemIngredientRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderItemRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments.PaymentRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.util.TrackingTokenGenerator;

import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PromotionService promotionService;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final OrderItemIngredientRepository orderItemIngredientRepository;
    private final PaymentService paymentService;
    
    @Value("${app.tracking.base-url:http://localhost:8080}")
    private String trackingBaseUrl;

    public OrderService(OrderRepository orderRepository, 
                       EmployeeRepository employeeRepository,
                       ProductRepository productRepository,
                       OrderItemRepository orderItemRepository,
                       PaymentRepository paymentRepository,
                       PromotionService promotionService,
                       IngredientRepository ingredientRepository,
                       ProductIngredientRepository productIngredientRepository,
                       OrderItemIngredientRepository orderItemIngredientRepository,
                       PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.promotionService = promotionService;
        this.ingredientRepository = ingredientRepository;
        this.productIngredientRepository = productIngredientRepository;
        this.orderItemIngredientRepository = orderItemIngredientRepository;
        this.paymentService = paymentService;
    }

    // ===== PHASE 1: ULTRA-SIMPLE ORDERS =====

    /**
     * Create an empty order (no items yet).
     */
    @Transactional
    public OrderDto createOrder(OrderType orderType, Long employeeId) {
        // Validate order type
        if (orderType == null) {
            throw new RuntimeException("Order type cannot be null");
        }

        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Create empty order
        Order order = Order.builder()
                .employee(employee)
                .status(OrderStatus.NEW)
                .orderType(orderType)
                .placedAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)  // Empty order = 0 price
                .orderItems(new ArrayList<>())  // Empty list
                .build();

        Order saved = orderRepository.save(order);
        return mapToDto(saved);
    }

    /**
     * Get all orders.
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a single order by ID.
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToDto(order);
    }

    /**
     * Create an order from client (public API, no authentication required).
     * Generates tracking token and optionally creates payment.
     */
    @Transactional
    public OrderDto createClientOrder(CreateClientOrderRequest request) {
        // Validate order type
        if (request.getOrderType() == null) {
            throw new RuntimeException("Order type cannot be null");
        }

        // Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        // Generate unique tracking token
        String trackingToken = TrackingTokenGenerator.generateToken();
        
        // Ensure token is unique (retry if collision, though very unlikely)
        while (orderRepository.findByTrackingToken(trackingToken).isPresent()) {
            trackingToken = TrackingTokenGenerator.generateToken();
        }

        // Create order without employee (client order)
        Order order = Order.builder()
                .employee(null)  // Client orders don't have employee
                .status(OrderStatus.NEW)
                .orderType(request.getOrderType())
                .placedAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)  // Will be recalculated after adding items
                .orderItems(new ArrayList<>())
                .trackingToken(trackingToken)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Add all items from request
        for (AddItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new RuntimeException("Invalid item: productId and quantity (positive) are required");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getBasePrice())
                    .status(OrderItemStatus.PENDING)
                    .build();

            orderItemRepository.save(orderItem);
            savedOrder.getOrderItems().add(orderItem);
        }

        // Recalculate total price (triggers will also update it)
        recalculateTotalPriceWithPromotions(savedOrder);
        savedOrder = orderRepository.save(savedOrder);

        // Create payment if payment method is specified
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().trim().isEmpty()) {
            String paymentMethod = request.getPaymentMethod().toLowerCase();
            if (!paymentMethod.equals("cash") && !paymentMethod.equals("card") && !paymentMethod.equals("online")) {
                throw new RuntimeException("Invalid payment method: " + request.getPaymentMethod() + 
                        ". Must be one of: cash, card, online");
            }

            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setOrderId(savedOrder.getId());
            paymentRequest.setAmount(savedOrder.getTotalPrice() != null ? savedOrder.getTotalPrice() : BigDecimal.ZERO);
            paymentRequest.setMethod(paymentMethod);
            paymentRequest.setCompanyDetails(null); // Client orders don't have company details initially

            try {
                // For cash, create pending payment. For card/online, process immediately
                if ("cash".equals(paymentMethod)) {
                    paymentService.createPayment(paymentRequest);
                } else {
                    // For card/online, create and process payment immediately
                    paymentService.createAndProcessPayment(paymentRequest);
                }
            } catch (Exception e) {
                logger.error("Failed to create payment for order {}: {}", savedOrder.getId(), e.getMessage());
                // Don't fail the order creation if payment fails - order can be paid later
            }
        }

        return mapToDtoWithTracking(savedOrder);
    }

    /**
     * Get order by tracking token (public access, no authentication required).
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderByTrackingToken(String token) {
        Order order = orderRepository.findByTrackingToken(token)
                .orElseThrow(() -> new RuntimeException("Order not found with tracking token: " + token));
        return mapToDtoWithTracking(order);
    }

    // ===== PHASE 2: ADD ORDER ITEMS =====

    /**
     * Add an item to an order.
     */
    @Transactional
    public OrderDto addItemToOrder(Long orderId, Long productId, Integer quantity) {
        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Check if product already in order
        OrderItem existingItem = order.getOrderItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            orderItemRepository.save(existingItem);
        } else {
            // Create new order item
            OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getBasePrice())
                    .status(OrderItemStatus.PENDING)
                    .build();
            orderItemRepository.save(newItem);
            order.getOrderItems().add(newItem);
        }

        // Recalculate total price with promotions
        recalculateTotalPriceWithPromotions(order);
        Order saved = orderRepository.save(order);
        
        return mapToDto(saved);
    }

    /**
     * Remove an item from an order.
     */
    @Transactional
    public OrderDto removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + itemId));

        // Verify item belongs to this order
        if (!item.getOrder().getId().equals(orderId)) {
            throw new RuntimeException("Item does not belong to this order");
        }

        order.getOrderItems().remove(item);
        orderItemRepository.delete(item);

        // Recalculate total price with promotions
        recalculateTotalPriceWithPromotions(order);
        Order saved = orderRepository.save(order);

        return mapToDto(saved);
    }

    /**
     * Update item quantity.
     */
    @Transactional
    public OrderDto updateItemQuantity(Long orderId, Long itemId, Integer newQuantity) {
        // Validate quantity
        if (newQuantity == null || newQuantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + itemId));

        // Verify item belongs to this order
        if (!item.getOrder().getId().equals(orderId)) {
            throw new RuntimeException("Item does not belong to this order");
        }

        item.setQuantity(newQuantity);
        orderItemRepository.save(item);

        // Recalculate total price with promotions
        recalculateTotalPriceWithPromotions(order);
        Order saved = orderRepository.save(order);

        return mapToDto(saved);
    }

    // ===== PROMOTION OPERATIONS =====

    /**
     * Apply promotion to order.
     */
    @Transactional
    public OrderDto applyPromotion(Long orderId, String code) {
        promotionService.applyPromotionToOrder(orderId, code);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return mapToDto(order);
    }

    /**
     * Remove promotion from order.
     */
    @Transactional
    public OrderDto removePromotion(Long orderId, Long promotionId) {
        promotionService.removePromotionFromOrder(orderId, promotionId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return mapToDto(order);
    }

    /**
     * Get promotions applied to order.
     */
    @Transactional(readOnly = true)
    public List<PromotionDto> getOrderPromotions(Long orderId) {
        return promotionService.getOrderPromotions(orderId);
    }

    // ===== PIZZA CONFIGURATION OPERATIONS =====

    /**
     * Add a custom pizza to order (created from scratch with selected ingredients).
     */
    @Transactional
    public OrderDto addCustomPizzaToOrder(Long orderId, CreateCustomPizzaRequest request) {
        // Validate request
        if (request.getCustomName() == null || request.getCustomName().trim().isEmpty()) {
            throw new RuntimeException("Custom pizza name is required");
        }
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new RuntimeException("At least one ingredient is required for custom pizza");
        }
        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Base price must be non-negative");
        }

        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Create order item with product_id = null (custom pizza)
        OrderItem customItem = OrderItem.builder()
                .order(order)
                .product(null) // Custom pizza has no base product
                .quantity(1) // Default quantity, can be changed later
                .unitPrice(request.getBasePrice())
                .status(OrderItemStatus.PENDING)
                .customName(request.getCustomName())
                .customDescription(request.getCustomDescription())
                .orderItemIngredients(new ArrayList<>())
                .build();

        OrderItem savedItem = orderItemRepository.save(customItem);

        // Create order item ingredients
        for (IngredientSelectionDto ingredientSelection : request.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(ingredientSelection.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientSelection.getIngredientId()));

            ModificationType modType = ModificationType.fromString(ingredientSelection.getModificationType());
            if (modType != ModificationType.ADDED) {
                // For custom pizza, all ingredients should be 'added'
                modType = ModificationType.ADDED;
            }

            OrderItemIngredient orderItemIngredient = OrderItemIngredient.builder()
                    .orderItem(savedItem)
                    .ingredient(ingredient)
                    .quantity(ingredientSelection.getQuantity())
                    .modificationType(modType)
                    .build();

            orderItemIngredientRepository.save(orderItemIngredient);
            savedItem.getOrderItemIngredients().add(orderItemIngredient);
        }

        order.getOrderItems().add(savedItem);

        // Recalculate total price with promotions
        recalculateTotalPriceWithPromotions(order);
        Order saved = orderRepository.save(order);

        return mapToDto(saved);
    }

    /**
     * Add a modified pizza to order (base product with added/removed ingredients).
     */
    @Transactional
    public OrderDto addModifiedPizzaToOrder(Long orderId, ModifyPizzaRequest request) {
        // Validate request
        if (request.getProductId() == null) {
            throw new RuntimeException("Product ID is required for modified pizza");
        }

        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Get base product
        Product baseProduct = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Create order item with base product
        OrderItem modifiedItem = OrderItem.builder()
                .order(order)
                .product(baseProduct)
                .quantity(1) // Default quantity
                .unitPrice(baseProduct.getBasePrice())
                .status(OrderItemStatus.PENDING)
                .customName(null) // Not a fully custom pizza
                .customDescription(null)
                .orderItemIngredients(new ArrayList<>())
                .build();

        OrderItem savedItem = orderItemRepository.save(modifiedItem);

        // Get base ingredients from product
        List<ProductIngredient> baseIngredients = productIngredientRepository.findByProductId(baseProduct.getId());

        // Create order item ingredients for base ingredients (excluding removed ones)
        List<Long> removedIds = request.getRemovedIngredientIds() != null ? request.getRemovedIngredientIds() : new ArrayList<>();
        for (ProductIngredient productIngredient : baseIngredients) {
            if (!removedIds.contains(productIngredient.getIngredientId())) {
                // This ingredient is kept (base)
                OrderItemIngredient orderItemIngredient = OrderItemIngredient.builder()
                        .orderItem(savedItem)
                        .ingredient(productIngredient.getIngredient())
                        .quantity(productIngredient.getQuantity())
                        .modificationType(ModificationType.BASE)
                        .build();

                orderItemIngredientRepository.save(orderItemIngredient);
                savedItem.getOrderItemIngredients().add(orderItemIngredient);
            }
        }

        // Add new ingredients
        if (request.getAddedIngredients() != null) {
            for (IngredientSelectionDto ingredientSelection : request.getAddedIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(ingredientSelection.getIngredientId())
                        .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientSelection.getIngredientId()));

                OrderItemIngredient orderItemIngredient = OrderItemIngredient.builder()
                        .orderItem(savedItem)
                        .ingredient(ingredient)
                        .quantity(ingredientSelection.getQuantity())
                        .modificationType(ModificationType.ADDED)
                        .build();

                orderItemIngredientRepository.save(orderItemIngredient);
                savedItem.getOrderItemIngredients().add(orderItemIngredient);
            }
        }

        // Calculate price: base price + additional cost for added ingredients (optional)
        // For now, we keep base price. Can be enhanced later to add cost for extra ingredients
        BigDecimal finalPrice = baseProduct.getBasePrice();
        savedItem.setUnitPrice(finalPrice);

        orderItemRepository.save(savedItem);
        order.getOrderItems().add(savedItem);

        // Recalculate total price with promotions
        recalculateTotalPriceWithPromotions(order);
        Order saved = orderRepository.save(order);

        return mapToDto(saved);
    }

    // --- Helper Methods ---

    private void recalculateTotalPrice(Order order) {
        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);
    }

    /**
     * Recalculate total price with promotions applied.
     */
    private void recalculateTotalPriceWithPromotions(Order order) {
        promotionService.recalculateOrderPrice(order);
    }

    // ===== PHASE 3: STATUS WORKFLOW =====

    /**
     * Update order status with validation.
     */
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus, Authentication authentication) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        
        // Validate courier restrictions
        if (authentication != null && isCourier(authentication)) {
            validateCourierStatusChange(currentStatus, newStatus, order.getOrderType());
        }
        
        validateOrderStatusTransition(currentStatus, newStatus);

        // Check payment status before completion (warning only, not blocking)
        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.COMPLETED) {
            checkPaymentBeforeCompletion(order);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        return mapToDto(saved);
    }

    /**
     * Update order item status. CRITICAL: PENDING → PREPARING triggers stock deduction via DB trigger!
     */
    @Transactional
    public OrderDto updateOrderItemStatus(Long orderId, Long itemId, OrderItemStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + itemId));

        // Verify item belongs to this order
        if (!item.getOrder().getId().equals(orderId)) {
            throw new RuntimeException("Item does not belong to this order");
        }

        OrderItemStatus currentStatus = item.getStatus();
        validateItemStatusTransition(currentStatus, newStatus);

        item.setStatus(newStatus);
        orderItemRepository.save(item);

        // Auto-update parent order status based on items
        updateOrderStatusBasedOnItems(order);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        return mapToDto(saved);
    }

    /**
     * Get orders filtered by status.
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Validation Methods ---

    /**
     * Check if the authenticated user is a courier.
     */
    private boolean isCourier(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_COURIER"));
    }

    /**
     * Validate that couriers can only mark orders as DELIVERED or COMPLETED from READY status.
     */
    private void validateCourierStatusChange(OrderStatus currentStatus, OrderStatus newStatus, OrderType orderType) {
        // Couriers can only change status from READY
        if (currentStatus != OrderStatus.READY) {
            throw new RuntimeException("Couriers can only mark orders as delivered/completed when order is READY. Current status: " + currentStatus);
        }

        // For DELIVERY orders, couriers can set to DELIVERED
        // For PICKUP/DINE_IN orders, couriers can set to COMPLETED
        if (orderType == OrderType.DELIVERY) {
            if (newStatus != OrderStatus.DELIVERED) {
                throw new RuntimeException("Couriers can only mark DELIVERY orders as DELIVERED. Attempted status: " + newStatus);
            }
        } else if (orderType == OrderType.PICKUP || orderType == OrderType.DINE_IN) {
            if (newStatus != OrderStatus.COMPLETED) {
                throw new RuntimeException("Couriers can only mark PICKUP/DINE_IN orders as COMPLETED. Attempted status: " + newStatus);
            }
        } else {
            throw new RuntimeException("Invalid order type for courier status update: " + orderType);
        }
    }

    private void validateOrderStatusTransition(OrderStatus current, OrderStatus target) {
        // Same status is always allowed (idempotent)
        if (current == target) {
            return;
        }

        // CANCELLED can be set from NEW, PREPARING, READY
        if (target == OrderStatus.CANCELLED) {
            if (current == OrderStatus.NEW || current == OrderStatus.PREPARING || current == OrderStatus.READY) {
                return;
            }
            throw new RuntimeException("Cannot cancel order from status: " + current);
        }

        // Final states cannot be changed
        if (current == OrderStatus.DELIVERED || current == OrderStatus.COMPLETED || 
            current == OrderStatus.CANCELLED || current == OrderStatus.FAILED) {
            throw new RuntimeException("Cannot change status from final state: " + current);
        }

        // Valid transitions
        switch (current) {
            case NEW:
                if (target == OrderStatus.PREPARING || target == OrderStatus.CANCELLED) {
                    return;
                }
                break;
            case PREPARING:
                if (target == OrderStatus.READY || target == OrderStatus.CANCELLED) {
                    return;
                }
                break;
            case READY:
                if (target == OrderStatus.DELIVERED || target == OrderStatus.COMPLETED || 
                    target == OrderStatus.CANCELLED) {
                    return;
                }
                break;
            default:
                break;
        }

        throw new RuntimeException("Invalid status transition from " + current + " to " + target);
    }

    private void validateItemStatusTransition(OrderItemStatus current, OrderItemStatus target) {
        // Same status is always allowed (idempotent)
        if (current == target) {
            return;
        }

        // CANCELLED can be set from PENDING or PREPARING
        if (target == OrderItemStatus.CANCELLED) {
            if (current == OrderItemStatus.PENDING || current == OrderItemStatus.PREPARING) {
                return;
            }
            throw new RuntimeException("Cannot cancel item from status: " + current);
        }

        // READY and CANCELLED are final states
        if (current == OrderItemStatus.READY || current == OrderItemStatus.CANCELLED) {
            throw new RuntimeException("Cannot change item status from final state: " + current);
        }

        // Valid transitions
        switch (current) {
            case PENDING:
                if (target == OrderItemStatus.PREPARING || target == OrderItemStatus.CANCELLED) {
                    return;
                }
                break;
            case PREPARING:
                if (target == OrderItemStatus.READY || target == OrderItemStatus.CANCELLED) {
                    return;
                }
                break;
            default:
                break;
        }

        throw new RuntimeException("Invalid item status transition from " + current + " to " + target);
    }

    /**
     * Auto-update order status based on aggregated item statuses.
     */
    private void updateOrderStatusBasedOnItems(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return;
        }

        List<OrderItem> items = order.getOrderItems();
        
        // Check if any item is PREPARING
        boolean anyPreparing = items.stream()
                .anyMatch(item -> item.getStatus() == OrderItemStatus.PREPARING);
        
        // Check if all items are READY
        boolean allReady = items.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.READY);
        
        // Check if any item is CANCELLED
        boolean anyCancelled = items.stream()
                .anyMatch(item -> item.getStatus() == OrderItemStatus.CANCELLED);

        OrderStatus currentStatus = order.getStatus();

        // If any item is PREPARING, order should be PREPARING (unless already in a later state)
        if (anyPreparing && (currentStatus == OrderStatus.NEW || currentStatus == OrderStatus.PREPARING)) {
            order.setStatus(OrderStatus.PREPARING);
            return;
        }

        // If all items are READY, order should be READY (unless already delivered/completed)
        if (allReady && (currentStatus == OrderStatus.NEW || currentStatus == OrderStatus.PREPARING || 
                         currentStatus == OrderStatus.READY)) {
            order.setStatus(OrderStatus.READY);
            return;
        }

        // If all items are CANCELLED, order should be CANCELLED
        boolean allCancelled = items.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.CANCELLED);
        if (allCancelled && currentStatus != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
        }
    }

    // --- Payment Validation ---

    /**
     * Check payment status before order completion.
     * Logs a warning if payment is not completed, but does not block the status transition.
     */
    private void checkPaymentBeforeCompletion(Order order) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(order.getId());
        if (paymentOpt.isEmpty() || !"paid".equals(paymentOpt.get().getStatus())) {
            logger.warn("Order {} is being completed without payment. Order ID: {}, Status: {}",
                    order.getId(), order.getId(), paymentOpt.map(p -> p.getStatus()).orElse("none"));
        }
    }

    // --- Mapper ---

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(this::mapItemToDto)
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        // Get payment information
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(order.getId());
        String paymentStatus = "none";
        Long paymentId = null;
        String paymentMethod = null;

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            paymentId = payment.getId();
            paymentStatus = payment.getStatus() != null ? payment.getStatus().toLowerCase() : "none";
            if ("paid".equals(paymentStatus)) {
                paymentMethod = payment.getMethod();
            }
        }

        // Get promotion information
        List<PromotionDto> appliedPromotions = promotionService.getOrderPromotions(order.getId());
        
        // Calculate base total (without promotions)
        BigDecimal baseTotal = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add) :
                BigDecimal.ZERO;
        
        // Calculate discount amount
        BigDecimal finalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal discountAmount = baseTotal.subtract(finalPrice);
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            discountAmount = BigDecimal.ZERO;
        }

        OrderDto.OrderDtoBuilder builder = OrderDto.builder()
                .id(order.getId())
                .employeeId(order.getEmployee() != null ? order.getEmployee().getId() : null)
                .employeeName(order.getEmployee() != null ?
                        order.getEmployee().getName() + " " + order.getEmployee().getLastName() : null)
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .placedAt(order.getPlacedAt())
                .updatedAt(order.getUpdatedAt())
                .totalPrice(baseTotal) // Base price without discount
                .items(itemDtos)
                .paymentStatus(paymentStatus)
                .paymentId(paymentId)
                .paymentMethod(paymentMethod)
                .appliedPromotions(appliedPromotions)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice);

        // Add tracking information if available
        if (order.getTrackingToken() != null) {
            builder.trackingToken(order.getTrackingToken())
                   .trackingUrl(trackingBaseUrl + "/api/orders/track/" + order.getTrackingToken());
        }

        return builder.build();
    }

    /**
     * Map Order to DTO with tracking information (same as mapToDto, kept for clarity).
     */
    private OrderDto mapToDtoWithTracking(Order order) {
        return mapToDto(order);
    }

    private OrderItemDto mapItemToDto(OrderItem item) {
        BigDecimal totalPrice = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        
        // Get ingredients from order_item_ingredients
        List<OrderItemIngredientDto> ingredientDtos = new ArrayList<>();
        if (item.getOrderItemIngredients() != null && !item.getOrderItemIngredients().isEmpty()) {
            ingredientDtos = item.getOrderItemIngredients().stream()
                    .map(this::mapOrderItemIngredientToDto)
                    .collect(Collectors.toList());
        } else {
            // Fallback: if no order_item_ingredients, try to get from product
            if (item.getProduct() != null && item.getProduct().getProductIngredients() != null) {
                ingredientDtos = item.getProduct().getProductIngredients().stream()
                        .map(pi -> OrderItemIngredientDto.builder()
                                .ingredientId(pi.getIngredientId())
                                .ingredientName(pi.getIngredient() != null ? pi.getIngredient().getName() : "Unknown")
                                .quantity(pi.getQuantity())
                                .modificationType("base")
                                .build())
                        .collect(Collectors.toList());
            }
        }
        
        OrderItemDto.OrderItemDtoBuilder builder = OrderItemDto.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(totalPrice)
                .status(item.getStatus())
                .customName(item.getCustomName())
                .customDescription(item.getCustomDescription())
                .ingredients(ingredientDtos)
                .isCustom(item.getProduct() == null);

        if (item.getProduct() != null) {
            builder.productId(item.getProduct().getId())
                   .productName(item.getProduct().getName());
        } else {
            builder.productId(null)
                   .productName(item.getCustomName() != null ? item.getCustomName() : "Custom Pizza");
        }

        return builder.build();
    }

    private OrderItemIngredientDto mapOrderItemIngredientToDto(OrderItemIngredient orderItemIngredient) {
        return OrderItemIngredientDto.builder()
                .ingredientId(orderItemIngredient.getIngredient().getId())
                .ingredientName(orderItemIngredient.getIngredient().getName())
                .quantity(orderItemIngredient.getQuantity())
                .modificationType(orderItemIngredient.getModificationType().getDbValue())
                .build();
    }
}

