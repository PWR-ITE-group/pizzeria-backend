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
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.AssignCourierRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreateDeliveryRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.DeliveryDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.DeliveryInfoDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdateDeliveryStatusRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.DeliveryService;

import java.util.List;

@Tag(name = "Deliveries", description = "Delivery management endpoints for managing deliveries, couriers, and delivery information")
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }


    @Operation(summary = "Create delivery", description = "Create a new delivery for an order", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<DeliveryDto> createDelivery(@RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.createDelivery(request));
    }

    @Operation(summary = "Get delivery by ID", description = "Retrieve delivery by ID", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'COURIER')")
    public ResponseEntity<DeliveryDto> getDeliveryById(
            @Parameter(description = "Delivery ID") @PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @Operation(summary = "Get delivery by order ID", description = "Retrieve delivery for a specific order", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'COURIER')")
    public ResponseEntity<DeliveryDto> getDeliveryByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId));
    }

    @Operation(summary = "Get all deliveries", description = "Retrieve all deliveries. Manager access only.", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<DeliveryDto>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @Operation(summary = "Assign courier to delivery", description = "Assign a courier to a delivery. Manager access only.", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Courier assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery or courier not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/assign-courier")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DeliveryDto> assignCourier(
            @Parameter(description = "Delivery ID") @PathVariable Long id,
            @RequestBody AssignCourierRequest request) {
        return ResponseEntity.ok(deliveryService.assignCourier(id, request.getCourierId()));
    }

    @Operation(summary = "Update delivery status", description = "Update delivery status. Manager and Courier access.", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'COURIER')")
    public ResponseEntity<DeliveryDto> updateDeliveryStatus(
            @Parameter(description = "Delivery ID") @PathVariable Long id,
            @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, request.getStatus()));
    }

    @Operation(summary = "Get deliveries by courier", description = "Retrieve all deliveries assigned to a specific courier", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Courier not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/courier/{courierId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'COURIER')")
    public ResponseEntity<List<DeliveryDto>> getDeliveriesByCourier(
            @Parameter(description = "Courier ID") @PathVariable Long courierId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByCourier(courierId));
    }

    @Operation(summary = "Get deliveries by status", description = "Retrieve deliveries filtered by status", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER', 'COURIER')")
    public ResponseEntity<List<DeliveryDto>> getDeliveriesByStatus(
            @Parameter(description = "Delivery status") @PathVariable String status) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(status));
    }


    @Operation(summary = "Add delivery information", description = "Add customer delivery information (address, contact) to a delivery", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery info added successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/info")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<DeliveryDto> addDeliveryInfo(
            @Parameter(description = "Delivery ID") @PathVariable Long id,
            @RequestBody DeliveryInfoDto deliveryInfoDto) {
        return ResponseEntity.ok(deliveryService.addDeliveryInfo(id, deliveryInfoDto));
    }

    @Operation(summary = "Update delivery information", description = "Update customer delivery information for a delivery", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery info updated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery or delivery info not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/info")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<DeliveryDto> updateDeliveryInfo(
            @Parameter(description = "Delivery ID") @PathVariable Long id,
            @RequestBody DeliveryInfoDto deliveryInfoDto) {
        return ResponseEntity.ok(deliveryService.updateDeliveryInfo(id, deliveryInfoDto));
    }

    @Operation(summary = "Get delivery information", description = "Retrieve customer delivery information for a delivery", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeliveryInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "Delivery or delivery info not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/info")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER', 'COURIER')")
    public ResponseEntity<DeliveryInfoDto> getDeliveryInfo(
            @Parameter(description = "Delivery ID") @PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryInfoByDeliveryId(id));
    }

    @Operation(summary = "Search delivery info by phone", description = "Search delivery information by customer phone number for autocomplete", tags = {"Deliveries"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/info/phone/{phone}")
    @PreAuthorize("hasAnyRole('MANAGER', 'WAITER')")
    public ResponseEntity<List<DeliveryInfoDto>> getDeliveryInfoByPhone(
            @Parameter(description = "Phone number") @PathVariable String phone) {
        return ResponseEntity.ok(deliveryService.getDeliveryInfoByPhone(phone));
    }
}

