package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreatePromotionRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PromotionDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdatePromotionRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.OrderPromotion;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.promotions.Promotion;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions.OrderPromotionRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.promotions.PromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final OrderRepository orderRepository;

    public PromotionService(PromotionRepository promotionRepository,
                          OrderPromotionRepository orderPromotionRepository,
                          OrderRepository orderRepository) {
        this.promotionRepository = promotionRepository;
        this.orderPromotionRepository = orderPromotionRepository;
        this.orderRepository = orderRepository;
    }

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new promotion.
     */
    @Transactional
    public PromotionDto createPromotion(CreatePromotionRequest request) {
        // Validate code uniqueness
        if (promotionRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Promotion code already exists: " + request.getCode());
        }

        // Validate discount percent
        if (request.getDiscountPercent() == null || 
            request.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0 ||
            request.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Discount percent must be between 0 and 100");
        }

        // Validate dates
        if (request.getValidFrom() != null && request.getValidTo() != null) {
            if (request.getValidFrom().isAfter(request.getValidTo())) {
                throw new RuntimeException("Valid from date must be before valid to date");
            }
        }

        Promotion promotion = Promotion.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .discountPercent(request.getDiscountPercent())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .isActive(true) // Default to active
                .build();

        Promotion saved = promotionRepository.save(promotion);
        return mapToDto(saved);
    }

    /**
     * Get all promotions.
     */
    @Transactional(readOnly = true)
    public List<PromotionDto> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active promotions (currently valid).
     */
    @Transactional(readOnly = true)
    public List<PromotionDto> getActivePromotions() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findByIsActiveTrue().stream()
                .filter(p -> {
                    if (p.getValidFrom() != null && today.isBefore(p.getValidFrom())) {
                        return false;
                    }
                    if (p.getValidTo() != null && today.isAfter(p.getValidTo())) {
                        return false;
                    }
                    return true;
                })
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get promotion by ID.
     */
    @Transactional(readOnly = true)
    public PromotionDto getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return mapToDto(promotion);
    }

    /**
     * Get promotion by code.
     */
    @Transactional(readOnly = true)
    public PromotionDto getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + code));
        return mapToDto(promotion);
    }

    /**
     * Update promotion.
     */
    @Transactional
    public PromotionDto updatePromotion(Long id, UpdatePromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }

        if (request.getDiscountPercent() != null) {
            if (request.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0 ||
                request.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("Discount percent must be between 0 and 100");
            }
            promotion.setDiscountPercent(request.getDiscountPercent());
        }

        if (request.getValidFrom() != null) {
            promotion.setValidFrom(request.getValidFrom());
        }

        if (request.getValidTo() != null) {
            promotion.setValidTo(request.getValidTo());
        }

        // Validate dates
        if (promotion.getValidFrom() != null && promotion.getValidTo() != null) {
            if (promotion.getValidFrom().isAfter(promotion.getValidTo())) {
                throw new RuntimeException("Valid from date must be before valid to date");
            }
        }

        if (request.getIsActive() != null) {
            promotion.setIsActive(request.getIsActive());
        }

        Promotion saved = promotionRepository.save(promotion);
        return mapToDto(saved);
    }

    /**
     * Delete promotion.
     */
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        // Check if promotion is used in any orders
        boolean isUsed = orderPromotionRepository.findAll().stream()
                .anyMatch(op -> op.getPromotion().getId().equals(id));

        if (isUsed) {
            throw new RuntimeException("Cannot delete promotion that is used in orders. Deactivate it instead.");
        }

        promotionRepository.deleteById(id);
    }

    /**
     * Activate promotion.
     */
    @Transactional
    public PromotionDto activatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        promotion.setIsActive(true);
        Promotion saved = promotionRepository.save(promotion);
        return mapToDto(saved);
    }

    /**
     * Deactivate promotion.
     */
    @Transactional
    public PromotionDto deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        promotion.setIsActive(false);
        Promotion saved = promotionRepository.save(promotion);
        return mapToDto(saved);
    }

    // ===== VALIDATION =====

    /**
     * Validate promotion code (check if it's valid for use).
     */
    @Transactional(readOnly = true)
    public PromotionDto validatePromotion(String code) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion code not found: " + code));

        if (!promotion.getIsActive()) {
            throw new RuntimeException("Promotion code is not active: " + code);
        }

        LocalDate today = LocalDate.now();
        if (promotion.getValidFrom() != null && today.isBefore(promotion.getValidFrom())) {
            throw new RuntimeException("Promotion code is not yet valid: " + code);
        }
        if (promotion.getValidTo() != null && today.isAfter(promotion.getValidTo())) {
            throw new RuntimeException("Promotion code has expired: " + code);
        }

        return mapToDto(promotion);
    }

    /**
     * Validate promotion for order (check if it can be applied to specific order).
     */
    @Transactional(readOnly = true)
    public void validatePromotionForOrder(String code, Long orderId) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion code not found: " + code));

        if (!promotion.getIsActive()) {
            throw new RuntimeException("Promotion code is not active: " + code);
        }

        LocalDate today = LocalDate.now();
        if (promotion.getValidFrom() != null && today.isBefore(promotion.getValidFrom())) {
            throw new RuntimeException("Promotion code is not yet valid: " + code);
        }
        if (promotion.getValidTo() != null && today.isAfter(promotion.getValidTo())) {
            throw new RuntimeException("Promotion code has expired: " + code);
        }

        if (orderPromotionRepository.existsByOrder_IdAndPromotion_Id(orderId, promotion.getId())) {
            throw new RuntimeException("Promotion code already applied to this order: " + code);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.COMPLETED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot apply promotion to completed/cancelled order");
        }
    }

    // ===== ORDER PROMOTION OPERATIONS =====

    /**
     * Apply promotion to order.
     */
    @Transactional
    public PromotionDto applyPromotionToOrder(Long orderId, String code) {
        // Validate promotion for order
        validatePromotionForOrder(code, orderId);

        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + code));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Create order-promotion link
        OrderPromotion orderPromotion = OrderPromotion.builder()
                .order(order)
                .promotion(promotion)
                .build();

        orderPromotionRepository.save(orderPromotion);

        // Recalculate order price
        recalculateOrderPrice(order);
        orderRepository.save(order);

        return mapToDto(promotion);
    }

    /**
     * Remove promotion from order.
     */
    @Transactional
    public void removePromotionFromOrder(Long orderId, Long promotionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.COMPLETED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot remove promotion from completed/cancelled order");
        }

        orderPromotionRepository.deleteByOrder_IdAndPromotion_Id(orderId, promotionId);

        // Recalculate order price
        recalculateOrderPrice(order);
        orderRepository.save(order);
    }

    /**
     * Get promotions applied to order.
     */
    @Transactional(readOnly = true)
    public List<PromotionDto> getOrderPromotions(Long orderId) {
        return orderPromotionRepository.findByOrder_Id(orderId).stream()
                .map(op -> mapToDto(op.getPromotion()))
                .collect(Collectors.toList());
    }

    /**
     * Recalculate order price with promotions.
     */
    @Transactional
    public void recalculateOrderPrice(Order order) {
        BigDecimal baseTotal = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add) :
                BigDecimal.ZERO;

        List<Promotion> promotions = orderPromotionRepository.findByOrder_Id(order.getId()).stream()
                .map(OrderPromotion::getPromotion)
                .collect(Collectors.toList());

        BigDecimal totalDiscountPercent = promotions.stream()
                .map(Promotion::getDiscountPercent)
                .filter(discount -> discount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Maximum discount 100%
        if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) {
            totalDiscountPercent = new BigDecimal("100");
        }

        BigDecimal discountAmount = baseTotal.multiply(totalDiscountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = baseTotal.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        order.setTotalPrice(finalPrice);
    }

    // --- Mapper ---

    private PromotionDto mapToDto(Promotion promotion) {
        return PromotionDto.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .description(promotion.getDescription())
                .discountPercent(promotion.getDiscountPercent())
                .validFrom(promotion.getValidFrom())
                .validTo(promotion.getValidTo())
                .isActive(promotion.getIsActive())
                .build();
    }
}

