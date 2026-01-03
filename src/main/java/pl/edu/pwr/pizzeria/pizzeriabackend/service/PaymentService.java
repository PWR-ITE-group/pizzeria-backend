package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreatePaymentCompanyDetailsRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreatePaymentRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PaymentCompanyDetailsDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PaymentDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdatePaymentCompanyDetailsRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.orders.Order;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.Payment;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.PaymentCompanyDetails;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.orders.OrderRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments.PaymentCompanyDetailsRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCompanyDetailsRepository paymentCompanyDetailsRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, 
                         PaymentCompanyDetailsRepository paymentCompanyDetailsRepository,
                         OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentCompanyDetailsRepository = paymentCompanyDetailsRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Create a payment for an order.
     */
    @Transactional
    public PaymentDto createPayment(CreatePaymentRequest request) {
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Payment already exists for order id: " + request.getOrderId());
        }

        // Validate amount matches order total (with small tolerance for rounding)
        BigDecimal orderTotal = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal amountDifference = request.getAmount().subtract(orderTotal).abs();
        if (amountDifference.compareTo(new BigDecimal("0.01")) > 0) {
            throw new RuntimeException(
                    "Payment amount does not match order total. Order total: " + orderTotal +
                    ", Payment amount: " + request.getAmount());
        }

        // Validate payment method
        String method = request.getMethod().toLowerCase();
        if (!method.equals("card") && !method.equals("cash") && !method.equals("online")) {
            throw new RuntimeException("Invalid payment method: " + request.getMethod() +
                    ". Must be one of: card, cash, online");
        }

        // Create payment
        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .method(method)
                .status("pending")
                .paidAt(null)
                .build();

        Payment saved = paymentRepository.save(payment);

        // Create company details if provided
        if (request.getCompanyDetails() != null) {
            createCompanyDetails(saved.getId(), request.getCompanyDetails());
        }

        return mapToDto(saved);
    }

    /**
     * Create payment and process it immediately (convenience method for frontend).
     */
    @Transactional
    public PaymentDto createAndProcessPayment(CreatePaymentRequest request) {
        // Create payment first
        PaymentDto paymentDto = createPayment(request);
        // Then process it
        return processPayment(paymentDto.getId());
    }

    /**
     * Update payment status.
     */
    @Transactional
    public PaymentDto updatePaymentStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        // Validate status
        String lowerStatus = status.toLowerCase();
        if (!lowerStatus.equals("pending") && !lowerStatus.equals("paid") && !lowerStatus.equals("failed")) {
            throw new RuntimeException("Invalid payment status: " + status +
                    ". Must be one of: pending, paid, failed");
        }

        // Validate status transition
        String currentStatus = payment.getStatus().toLowerCase();
        if (currentStatus.equals("paid") && !lowerStatus.equals("paid")) {
            throw new RuntimeException("Cannot change status of already paid payment");
        }

        payment.setStatus(lowerStatus);

        // Set paid_at when status is paid
        if (lowerStatus.equals("paid")) {
            payment.setPaidAt(LocalDateTime.now());
        } else if (lowerStatus.equals("pending")) {
            payment.setPaidAt(null);
        }

        Payment saved = paymentRepository.save(payment);
        return mapToDto(saved);
    }

    /**
     * Mark payment as paid (convenience method).
     */
    @Transactional
    public PaymentDto markPaymentAsPaid(Long paymentId) {
        return updatePaymentStatus(paymentId, "paid");
    }

    /**
     * Process payment (mock payment processing).
     * Simulates payment processing with 1-2 second delay and 95% success rate.
     */
    @Transactional
    public PaymentDto processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        // Allow processing of pending or failed payments (retry failed payments)
        String currentStatus = payment.getStatus().toLowerCase();
        if (!"pending".equals(currentStatus) && !"failed".equals(currentStatus)) {
            throw new RuntimeException("Payment already processed. Current status: " + payment.getStatus());
        }

        // Simulate payment processing delay (1-2 seconds)
        try {
            Thread.sleep(1000 + (long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted", e);
        }

        // 95% success rate, 5% failure
        boolean success = Math.random() < 0.95;
        String newStatus = success ? "paid" : "failed";

        payment.setStatus(newStatus);
        if (success) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment saved = paymentRepository.save(payment);
        return mapToDto(saved);
    }

    /**
     * Get payment by ID.
     */
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return mapToDto(payment);
    }

    /**
     * Get payment by order ID.
     */
    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order id: " + orderId));
        return mapToDto(payment);
    }

    /**
     * Get all payments.
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get payments by status.
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByStatus(String status) {
        String lowerStatus = status.toLowerCase();
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getStatus().toLowerCase().equals(lowerStatus))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get payments by method.
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByMethod(String method) {
        String lowerMethod = method.toLowerCase();
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getMethod().toLowerCase().equals(lowerMethod))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ===== PAYMENT COMPANY DETAILS OPERATIONS =====

    /**
     * Add company details to payment.
     */
    @Transactional
    public PaymentCompanyDetailsDto addCompanyDetails(Long paymentId, CreatePaymentCompanyDetailsRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        // Check if company details already exist
        if (paymentCompanyDetailsRepository.existsByPayment_Id(paymentId)) {
            throw new RuntimeException("Company details already exist for this payment. Use update instead.");
        }

        // Validate required fields
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }
        if (request.getNip() == null || request.getNip().trim().isEmpty()) {
            throw new RuntimeException("NIP is required");
        }

        PaymentCompanyDetails companyDetails = PaymentCompanyDetails.builder()
                .payment(payment)
                .companyName(request.getCompanyName())
                .nip(request.getNip())
                .street(request.getStreet())
                .houseNr(request.getHouseNr())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .accountNumber(request.getAccountNumber())
                .build();

        PaymentCompanyDetails saved = paymentCompanyDetailsRepository.save(companyDetails);
        return mapCompanyDetailsToDto(saved);
    }

    /**
     * Update company details for payment.
     */
    @Transactional
    public PaymentCompanyDetailsDto updateCompanyDetails(Long paymentId, UpdatePaymentCompanyDetailsRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        PaymentCompanyDetails companyDetails = paymentCompanyDetailsRepository.findByPayment_Id(paymentId)
                .orElseThrow(() -> new RuntimeException("Company details not found for this payment. Use add instead."));

        // Update fields (partial update - only non-null fields)
        if (request.getCompanyName() != null) {
            companyDetails.setCompanyName(request.getCompanyName());
        }
        if (request.getNip() != null) {
            companyDetails.setNip(request.getNip());
        }
        if (request.getStreet() != null) {
            companyDetails.setStreet(request.getStreet());
        }
        if (request.getHouseNr() != null) {
            companyDetails.setHouseNr(request.getHouseNr());
        }
        if (request.getCity() != null) {
            companyDetails.setCity(request.getCity());
        }
        if (request.getPostalCode() != null) {
            companyDetails.setPostalCode(request.getPostalCode());
        }
        if (request.getAccountNumber() != null) {
            companyDetails.setAccountNumber(request.getAccountNumber());
        }

        PaymentCompanyDetails saved = paymentCompanyDetailsRepository.save(companyDetails);
        return mapCompanyDetailsToDto(saved);
    }

    /**
     * Get company details for payment.
     */
    @Transactional(readOnly = true)
    public PaymentCompanyDetailsDto getCompanyDetails(Long paymentId) {
        PaymentCompanyDetails companyDetails = paymentCompanyDetailsRepository.findByPayment_Id(paymentId)
                .orElseThrow(() -> new RuntimeException("Company details not found for payment id: " + paymentId));
        return mapCompanyDetailsToDto(companyDetails);
    }

    /**
     * Delete company details for payment.
     */
    @Transactional
    public void deleteCompanyDetails(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        PaymentCompanyDetails companyDetails = paymentCompanyDetailsRepository.findByPayment_Id(paymentId)
                .orElseThrow(() -> new RuntimeException("Company details not found for this payment"));

        paymentCompanyDetailsRepository.delete(companyDetails);
    }

    /**
     * Create company details (internal method used by createPayment).
     */
    private void createCompanyDetails(Long paymentId, CreatePaymentCompanyDetailsRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        // Validate required fields
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }
        if (request.getNip() == null || request.getNip().trim().isEmpty()) {
            throw new RuntimeException("NIP is required");
        }

        PaymentCompanyDetails companyDetails = PaymentCompanyDetails.builder()
                .payment(payment)
                .companyName(request.getCompanyName())
                .nip(request.getNip())
                .street(request.getStreet())
                .houseNr(request.getHouseNr())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .accountNumber(request.getAccountNumber())
                .build();

        paymentCompanyDetailsRepository.save(companyDetails);
    }

    // --- Mapper ---

    private PaymentDto mapToDto(Payment payment) {
        PaymentCompanyDetailsDto companyDetailsDto = paymentCompanyDetailsRepository.findByPayment_Id(payment.getId())
                .map(this::mapCompanyDetailsToDto)
                .orElse(null);

        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .companyDetails(companyDetailsDto)
                .build();
    }

    private PaymentCompanyDetailsDto mapCompanyDetailsToDto(PaymentCompanyDetails companyDetails) {
        return PaymentCompanyDetailsDto.builder()
                .id(companyDetails.getId())
                .paymentId(companyDetails.getPayment() != null ? companyDetails.getPayment().getId() : null)
                .companyName(companyDetails.getCompanyName())
                .nip(companyDetails.getNip())
                .street(companyDetails.getStreet())
                .houseNr(companyDetails.getHouseNr())
                .city(companyDetails.getCity())
                .postalCode(companyDetails.getPostalCode())
                .accountNumber(companyDetails.getAccountNumber())
                .build();
    }
}

