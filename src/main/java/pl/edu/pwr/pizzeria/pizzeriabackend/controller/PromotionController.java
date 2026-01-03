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
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ApplyPromotionRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.CreatePromotionRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PromotionDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.UpdatePromotionRequest;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.PromotionService;

import java.util.List;

@Tag(name = "Promotions", description = "Promotion management endpoints for creating, managing, and validating promotions")
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // ===== CRUD OPERATIONS =====

    // 1. Create promotion
    @Operation(
            summary = "Create promotion",
            description = "Create a new promotion code. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion created successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PromotionDto> createPromotion(@RequestBody CreatePromotionRequest request) {
        return ResponseEntity.ok(promotionService.createPromotion(request));
    }

    // 2. Get all promotions
    @Operation(
            summary = "Get all promotions",
            description = "Retrieve all promotions including inactive ones. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    // 3. Get active promotions (public)
    @Operation(
            summary = "Get active promotions (public)",
            description = "Retrieve all active promotions. No authentication required.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active promotions retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<List<PromotionDto>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }

    // 4. Get promotion by ID
    @Operation(
            summary = "Get promotion by ID",
            description = "Retrieve a specific promotion by ID. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "404", description = "Promotion not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PromotionDto> getPromotionById(
            @Parameter(description = "Promotion ID") @PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    // 5. Get promotion by code (public for validation)
    @Operation(
            summary = "Get promotion by code (public)",
            description = "Retrieve promotion information by code. No authentication required.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "404", description = "Promotion not found")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<PromotionDto> getPromotionByCode(
            @Parameter(description = "Promotion code") @PathVariable String code) {
        return ResponseEntity.ok(promotionService.getPromotionByCode(code));
    }

    // 6. Update promotion
    @Operation(
            summary = "Update promotion",
            description = "Update promotion information. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion updated successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "404", description = "Promotion not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PromotionDto> updatePromotion(
            @Parameter(description = "Promotion ID") @PathVariable Long id,
            @RequestBody UpdatePromotionRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, request));
    }

    // 7. Delete promotion
    @Operation(
            summary = "Delete promotion",
            description = "Delete a promotion from the system. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Promotion not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deletePromotion(
            @Parameter(description = "Promotion ID") @PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok().build();
    }

    // 8. Activate promotion
    @Operation(
            summary = "Activate promotion",
            description = "Activate a promotion. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion activated successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "404", description = "Promotion not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PromotionDto> activatePromotion(
            @Parameter(description = "Promotion ID") @PathVariable Long id) {
        return ResponseEntity.ok(promotionService.activatePromotion(id));
    }

    // 9. Deactivate promotion
    @Operation(
            summary = "Deactivate promotion",
            description = "Deactivate a promotion. Manager access only.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion deactivated successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "404", description = "Promotion not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PromotionDto> deactivatePromotion(
            @Parameter(description = "Promotion ID") @PathVariable Long id) {
        return ResponseEntity.ok(promotionService.deactivatePromotion(id));
    }

    // 10. Validate promotion code (public)
    @Operation(
            summary = "Validate promotion code (public)",
            description = "Validate a promotion code and check if it can be applied to an order. No authentication required.",
            tags = {"Promotions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion validated successfully",
                    content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired promotion code"),
            @ApiResponse(responseCode = "404", description = "Promotion not found")
    })
    @PostMapping("/validate")
    public ResponseEntity<PromotionDto> validatePromotion(@RequestBody ApplyPromotionRequest request) {
        return ResponseEntity.ok(promotionService.validatePromotion(request.getCode()));
    }
}

