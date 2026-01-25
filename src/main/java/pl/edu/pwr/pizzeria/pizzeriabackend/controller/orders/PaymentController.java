package pl.edu.pwr.pizzeria.pizzeriabackend.controller.orders;

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
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreatePaymentCompanyDetailsRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreatePaymentRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PaymentCompanyDetailsDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PaymentDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdatePaymentCompanyDetailsRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdatePaymentStatusRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.PaymentService;

import java.util.List;

@Tag(name = "Payments", description = "Payment management endpoints for processing payments and managing company details (invoices)")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Create payment",
            description = "Create a new payment for an order. Company details (invoice) are optional and can be added later.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or amount mismatch"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> createPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @Operation(
            summary = "Get payment by ID",
            description = "Retrieve payment information by payment ID, including company details if available.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(
            summary = "Get payment by order ID",
            description = "Retrieve payment information for a specific order.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found for this order"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @Operation(
            summary = "Get all payments",
            description = "Retrieve all payments in the system. Manager access only.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @Operation(
            summary = "Update payment status",
            description = "Update the status of a payment. Valid statuses: pending, paid, failed.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or status transition"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> updatePaymentStatus(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @RequestBody UpdatePaymentStatusRequest request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, request.getStatus()));
    }

    @Operation(
            summary = "Mark payment as paid",
            description = "Convenience endpoint to mark a payment as paid. Equivalent to updating status to 'paid'.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment marked as paid",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> markPaymentAsPaid(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.markPaymentAsPaid(id));
    }

    @Operation(
            summary = "Process payment",
            description = "Process a payment using mock payment gateway. Simulates 1-2 second delay with 95% success rate. " +
                    "Can retry failed payments.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processed",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Payment already processed"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> processPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.processPayment(id));
    }

    @Operation(
            summary = "Create and process payment",
            description = "Create a payment for an order and immediately process it. Convenience endpoint for frontend.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment created and processed",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/order/{orderId}/process")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentDto> createAndProcessPayment(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @RequestBody CreatePaymentRequest request) {
        request.setOrderId(orderId);
        return ResponseEntity.ok(paymentService.createAndProcessPayment(request));
    }

    @Operation(
            summary = "Get payments by status",
            description = "Retrieve all payments filtered by status. Valid statuses: pending, paid, failed. Manager access only.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<PaymentDto>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @Operation(
            summary = "Get payments by method",
            description = "Retrieve all payments filtered by payment method. Valid methods: card, cash, online. Manager access only.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/method/{method}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<PaymentDto>> getPaymentsByMethod(
            @Parameter(description = "Payment method") @PathVariable String method) {
        return ResponseEntity.ok(paymentService.getPaymentsByMethod(method));
    }


    @Operation(
            summary = "Add company details (invoice)",
            description = "Add company details (invoice information) to a payment. Company name and NIP are required.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company details added successfully",
                    content = @Content(schema = @Schema(implementation = PaymentCompanyDetailsDto.class))),
            @ApiResponse(responseCode = "400", description = "Company details already exist or invalid data"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/company-details")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentCompanyDetailsDto> addCompanyDetails(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @RequestBody CreatePaymentCompanyDetailsRequest request) {
        return ResponseEntity.ok(paymentService.addCompanyDetails(id, request));
    }

    @Operation(
            summary = "Update company details (invoice)",
            description = "Update company details for a payment. Partial update supported - only provided fields will be updated.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company details updated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentCompanyDetailsDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment or company details not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/company-details")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentCompanyDetailsDto> updateCompanyDetails(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @RequestBody UpdatePaymentCompanyDetailsRequest request) {
        return ResponseEntity.ok(paymentService.updateCompanyDetails(id, request));
    }

    @Operation(
            summary = "Get company details (invoice)",
            description = "Retrieve company details (invoice information) for a payment.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentCompanyDetailsDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment or company details not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/company-details")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<PaymentCompanyDetailsDto> getCompanyDetails(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getCompanyDetails(id));
    }

    @Operation(
            summary = "Delete company details (invoice)",
            description = "Delete company details (invoice information) from a payment.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Company details deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payment or company details not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/company-details")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<Void> deleteCompanyDetails(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        paymentService.deleteCompanyDetails(id);
        return ResponseEntity.noContent().build();
    }
}

