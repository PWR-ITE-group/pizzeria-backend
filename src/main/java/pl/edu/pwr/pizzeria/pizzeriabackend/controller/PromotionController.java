package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.PromotionVerifyDTO;

@RestController
@RequestMapping("/api/v1")
public class PromotionController {

    // @Autowired PromotionService promotionService;

    // --- Publiczny endpoint dla klienta ---
    @PostMapping("/promotions/verify")
    public ResponseEntity<Object> verifyPromotion(@RequestBody PromotionVerifyDTO request) {
        // TODO: Wywołanie promotionService.verifyCode(request.code, request.basket)
        // TODO: Logika sprawdzania, czy kod jest ważny, aktywny i pasuje do koszyka
        return ResponseEntity.ok(new Object());
    }

    // --- Pracowniczy endpoint (MANAGER) ---
    @PostMapping("/manager/promotions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> createPromotion(@RequestBody Object promotionDetails) {
        // TODO: Walidacja i zapis nowej promocji
        return ResponseEntity.ok().build();
    }
}