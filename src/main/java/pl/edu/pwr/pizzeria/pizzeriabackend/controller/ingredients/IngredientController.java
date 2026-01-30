package pl.edu.pwr.pizzeria.pizzeriabackend.controller.ingredients;

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
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.IngredientDto;
import pl.edu.pwr.pizzeria.pizzeriabackend.service.IngredientService;

import java.util.List;

@Tag(name = "Ingredients", description = "Ingredient management endpoints for managing ingredients and their properties")
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Operation(
            summary = "Get public ingredients",
            description = "Retrieve all ingredients for pizza configuration. No authentication required.",
            tags = {"Ingredients"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredients retrieved successfully")
    })
    @GetMapping("/public")
    public ResponseEntity<List<IngredientDto>> getPublicIngredients() {
        return ResponseEntity.ok(ingredientService.getPublicIngredients());
    }

    @Operation(
            summary = "Get all ingredients",
            description = "Retrieve all ingredients with stock information. Manager and Chef access.",
            tags = {"Ingredients"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredients retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CHEF')")
    public ResponseEntity<List<IngredientDto>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    @Operation(
            summary = "Create ingredient",
            description = "Create a new ingredient in the system. Manager access only.",
            tags = {"Ingredients"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient created successfully",
                    content = @Content(schema = @Schema(implementation = IngredientDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<IngredientDto> createIngredient(@RequestBody IngredientDto dto) {
        return ResponseEntity.ok(ingredientService.createIngredient(dto));
    }

    @Operation(
            summary = "Update ingredient",
            description = "Update ingredient information. Manager access only.",
            tags = {"Ingredients"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient updated successfully",
                    content = @Content(schema = @Schema(implementation = IngredientDto.class))),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<IngredientDto> updateIngredient(
            @Parameter(description = "Ingredient ID") @PathVariable Long id,
            @RequestBody IngredientDto dto) {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, dto));
    }

    @Operation(
            summary = "Delete ingredient",
            description = "Delete an ingredient from the system. Manager access only.",
            tags = {"Ingredients"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredient deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteIngredient(
            @Parameter(description = "Ingredient ID") @PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.ok().build();
    }
}
