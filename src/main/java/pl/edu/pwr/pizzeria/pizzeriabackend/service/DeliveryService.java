package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateDeliveryRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.DeliveryDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.DeliveryInfoDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.Delivery;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.DeliveryInfo;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries.DeliveryInfoRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries.DeliveryRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            DeliveryInfoRepository deliveryInfoRepository,
            OrderRepository orderRepository,
            EmployeeRepository employeeRepository) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryInfoRepository = deliveryInfoRepository;
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new delivery for an order.
     * Only for DELIVERY order type.
     */
    @Transactional
    public DeliveryDto createDelivery(CreateDeliveryRequest request) {
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // Validate order type is DELIVERY
        if (order.getOrderType() != OrderType.DELIVERY) {
            throw new RuntimeException("Delivery can only be created for DELIVERY order type. Current type: " + order.getOrderType());
        }

        // Check if delivery already exists for this order
        if (deliveryRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Delivery already exists for order id: " + request.getOrderId());
        }

        // Validate courier exists and is a COURIER
        Employee courier = null;
        if (request.getCourierId() != null) {
            courier = employeeRepository.findById(request.getCourierId())
                    .orElseThrow(() -> new RuntimeException("Courier not found with id: " + request.getCourierId()));
            
            if (!"COURIER".equalsIgnoreCase(courier.getRole())) {
                throw new RuntimeException("Employee is not a COURIER. Role: " + courier.getRole());
            }
        }

        // Create delivery
        Delivery delivery = Delivery.builder()
                .order(order)
                .courier(courier)
                .status(courier != null ? "assigned" : "pending")
                .assignedAt(courier != null ? LocalDateTime.now() : null)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Create delivery info if provided
        DeliveryInfo deliveryInfo = null;
        if (request.getDeliveryInfo() != null) {
            deliveryInfo = DeliveryInfo.builder()
                    .delivery(savedDelivery)
                    .name(request.getDeliveryInfo().getName())
                    .lastName(request.getDeliveryInfo().getLastName())
                    .phone(request.getDeliveryInfo().getPhone())
                    .email(request.getDeliveryInfo().getEmail())
                    .street(request.getDeliveryInfo().getStreet())
                    .houseNr(request.getDeliveryInfo().getHouseNr())
                    .apartmentNr(request.getDeliveryInfo().getApartmentNr())
                    .city(request.getDeliveryInfo().getCity())
                    .postalCode(request.getDeliveryInfo().getPostalCode())
                    .additionalInfo(request.getDeliveryInfo().getAdditionalInfo())
                    .build();
            deliveryInfo = deliveryInfoRepository.save(deliveryInfo);
        }

        return mapToDto(savedDelivery, deliveryInfo);
    }

    /**
     * Assign a courier to a delivery.
     */
    @Transactional
    public DeliveryDto assignCourier(Long deliveryId, Long courierId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        // Validate courier exists and is a COURIER
        Employee courier = employeeRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found with id: " + courierId));

        if (!"COURIER".equalsIgnoreCase(courier.getRole())) {
            throw new RuntimeException("Employee is not a COURIER. Role: " + courier.getRole());
        }

        delivery.setCourier(courier);
        delivery.setStatus("assigned");
        delivery.setAssignedAt(LocalDateTime.now());

        Delivery saved = deliveryRepository.save(delivery);
        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(saved.getId()).orElse(null);

        return mapToDto(saved, deliveryInfo);
    }

    /**
     * Update delivery status.
     */
    @Transactional
    public DeliveryDto updateDeliveryStatus(Long deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        // Validate status
        if (!status.equals("assigned") && !status.equals("in_transit") && !status.equals("delivered")) {
            throw new RuntimeException("Invalid delivery status: " + status + ". Must be: assigned, in_transit, delivered");
        }

        // Validate status transition
        String currentStatus = delivery.getStatus();
        if (currentStatus.equals("delivered")) {
            throw new RuntimeException("Cannot change status of already delivered order");
        }

        delivery.setStatus(status);

        // Set delivered_at when status is delivered
        if (status.equals("delivered")) {
            delivery.setDeliveredAt(LocalDateTime.now());
            // Also update order status to DELIVERED
            Order order = delivery.getOrder();
            if (order != null && order.getStatus() != OrderStatus.DELIVERED) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }
        }

        Delivery saved = deliveryRepository.save(delivery);
        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(saved.getId()).orElse(null);

        return mapToDto(saved, deliveryInfo);
    }

    /**
     * Get delivery by ID.
     */
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + id));
        
        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(id).orElse(null);
        return mapToDto(delivery, deliveryInfo);
    }

    /**
     * Get delivery by order ID.
     */
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order id: " + orderId));
        
        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(delivery.getId()).orElse(null);
        return mapToDto(delivery, deliveryInfo);
    }

    /**
     * Get all deliveries.
     */
    @Transactional(readOnly = true)
    public List<DeliveryDto> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(delivery -> {
                    DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(delivery.getId()).orElse(null);
                    return mapToDto(delivery, deliveryInfo);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get deliveries by courier ID.
     */
    @Transactional(readOnly = true)
    public List<DeliveryDto> getDeliveriesByCourier(Long courierId) {
        return deliveryRepository.findAll().stream()
                .filter(delivery -> delivery.getCourier() != null && delivery.getCourier().getId().equals(courierId))
                .map(delivery -> {
                    DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(delivery.getId()).orElse(null);
                    return mapToDto(delivery, deliveryInfo);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get deliveries by status.
     */
    @Transactional(readOnly = true)
    public List<DeliveryDto> getDeliveriesByStatus(String status) {
        return deliveryRepository.findAll().stream()
                .filter(delivery -> delivery.getStatus().equals(status))
                .map(delivery -> {
                    DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(delivery.getId()).orElse(null);
                    return mapToDto(delivery, deliveryInfo);
                })
                .collect(Collectors.toList());
    }

    // ===== DELIVERY INFO OPERATIONS =====

    /**
     * Add DeliveryInfo to existing delivery.
     */
    @Transactional
    public DeliveryDto addDeliveryInfo(Long deliveryId, DeliveryInfoDto deliveryInfoDto) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        // Check if DeliveryInfo already exists
        if (deliveryInfoRepository.findByDelivery_Id(deliveryId).isPresent()) {
            throw new RuntimeException("DeliveryInfo already exists for this delivery. Use update instead.");
        }

        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .delivery(delivery)
                .name(deliveryInfoDto.getName())
                .lastName(deliveryInfoDto.getLastName())
                .phone(deliveryInfoDto.getPhone())
                .email(deliveryInfoDto.getEmail())
                .street(deliveryInfoDto.getStreet())
                .houseNr(deliveryInfoDto.getHouseNr())
                .apartmentNr(deliveryInfoDto.getApartmentNr())
                .city(deliveryInfoDto.getCity())
                .postalCode(deliveryInfoDto.getPostalCode())
                .additionalInfo(deliveryInfoDto.getAdditionalInfo())
                .build();

        deliveryInfoRepository.save(deliveryInfo);
        return mapToDto(delivery, deliveryInfo);
    }

    /**
     * Update DeliveryInfo for delivery.
     */
    @Transactional
    public DeliveryDto updateDeliveryInfo(Long deliveryId, DeliveryInfoDto deliveryInfoDto) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(deliveryId)
                .orElseThrow(() -> new RuntimeException("DeliveryInfo not found for this delivery. Use add instead."));

        // Update all fields
        if (deliveryInfoDto.getName() != null) {
            deliveryInfo.setName(deliveryInfoDto.getName());
        }
        if (deliveryInfoDto.getLastName() != null) {
            deliveryInfo.setLastName(deliveryInfoDto.getLastName());
        }
        if (deliveryInfoDto.getPhone() != null) {
            deliveryInfo.setPhone(deliveryInfoDto.getPhone());
        }
        if (deliveryInfoDto.getEmail() != null) {
            deliveryInfo.setEmail(deliveryInfoDto.getEmail());
        }
        if (deliveryInfoDto.getStreet() != null) {
            deliveryInfo.setStreet(deliveryInfoDto.getStreet());
        }
        if (deliveryInfoDto.getHouseNr() != null) {
            deliveryInfo.setHouseNr(deliveryInfoDto.getHouseNr());
        }
        if (deliveryInfoDto.getApartmentNr() != null) {
            deliveryInfo.setApartmentNr(deliveryInfoDto.getApartmentNr());
        }
        if (deliveryInfoDto.getCity() != null) {
            deliveryInfo.setCity(deliveryInfoDto.getCity());
        }
        if (deliveryInfoDto.getPostalCode() != null) {
            deliveryInfo.setPostalCode(deliveryInfoDto.getPostalCode());
        }
        if (deliveryInfoDto.getAdditionalInfo() != null) {
            deliveryInfo.setAdditionalInfo(deliveryInfoDto.getAdditionalInfo());
        }

        deliveryInfoRepository.save(deliveryInfo);
        return mapToDto(delivery, deliveryInfo);
    }

    /**
     * Get DeliveryInfo by phone (for autocomplete).
     */
    @Transactional(readOnly = true)
    public List<DeliveryInfoDto> getDeliveryInfoByPhone(String phone) {
        return deliveryInfoRepository.findByPhone(phone).stream()
                .map(this::mapDeliveryInfoToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get DeliveryInfo by delivery ID.
     */
    @Transactional(readOnly = true)
    public DeliveryInfoDto getDeliveryInfoByDeliveryId(Long deliveryId) {
        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByDelivery_Id(deliveryId)
                .orElseThrow(() -> new RuntimeException("DeliveryInfo not found for delivery id: " + deliveryId));
        return mapDeliveryInfoToDto(deliveryInfo);
    }

    // --- Mapper ---

    private DeliveryDto mapToDto(Delivery delivery, DeliveryInfo deliveryInfo) {
        DeliveryInfoDto deliveryInfoDto = null;
        if (deliveryInfo != null) {
            deliveryInfoDto = DeliveryInfoDto.builder()
                    .id(deliveryInfo.getId())
                    .name(deliveryInfo.getName())
                    .lastName(deliveryInfo.getLastName())
                    .phone(deliveryInfo.getPhone())
                    .email(deliveryInfo.getEmail())
                    .street(deliveryInfo.getStreet())
                    .houseNr(deliveryInfo.getHouseNr())
                    .apartmentNr(deliveryInfo.getApartmentNr())
                    .city(deliveryInfo.getCity())
                    .postalCode(deliveryInfo.getPostalCode())
                    .additionalInfo(deliveryInfo.getAdditionalInfo())
                    .build();
        }

        return DeliveryDto.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder() != null ? delivery.getOrder().getId() : null)
                .courierId(delivery.getCourier() != null ? delivery.getCourier().getId() : null)
                .courierName(delivery.getCourier() != null ?
                        delivery.getCourier().getName() + " " + delivery.getCourier().getLastName() : null)
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .deliveredAt(delivery.getDeliveredAt())
                .deliveryInfo(deliveryInfoDto)
                .build();
    }

    private DeliveryInfoDto mapDeliveryInfoToDto(DeliveryInfo deliveryInfo) {
        return DeliveryInfoDto.builder()
                .id(deliveryInfo.getId())
                .name(deliveryInfo.getName())
                .lastName(deliveryInfo.getLastName())
                .phone(deliveryInfo.getPhone())
                .email(deliveryInfo.getEmail())
                .street(deliveryInfo.getStreet())
                .houseNr(deliveryInfo.getHouseNr())
                .apartmentNr(deliveryInfo.getApartmentNr())
                .city(deliveryInfo.getCity())
                .postalCode(deliveryInfo.getPostalCode())
                .additionalInfo(deliveryInfo.getAdditionalInfo())
                .build();
    }
}

