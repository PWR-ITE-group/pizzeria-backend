package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateOrderRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderItemDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.OrderItem;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.products.Product;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderItemRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, 
                       EmployeeRepository employeeRepository,
                       ProductRepository productRepository,
                       OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
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

        // Recalculate total price
        recalculateTotalPrice(order);
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

        // Recalculate total price
        recalculateTotalPrice(order);
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

        // Recalculate total price
        recalculateTotalPrice(order);
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

    // --- Mapper ---

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(this::mapItemToDto)
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return OrderDto.builder()
                .id(order.getId())
                .employeeId(order.getEmployee() != null ? order.getEmployee().getId() : null)
                .employeeName(order.getEmployee() != null ?
                        order.getEmployee().getName() + " " + order.getEmployee().getLastName() : null)
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .placedAt(order.getPlacedAt())
                .updatedAt(order.getUpdatedAt())
                .totalPrice(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO)
                .items(itemDtos)
                .build();
    }

    private OrderItemDto mapItemToDto(OrderItem item) {
        BigDecimal totalPrice = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(totalPrice)
                .status(item.getStatus())
                .build();
    }
}

