package pl.edu.pwr.pizzeria.pizzeriabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.ProductDetailsDTO;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    // @Autowired ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductDetailsDTO>> getAvailableProducts() {
        // TODO: Wywołanie productService.getAvailableProducts()
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDetailsDTO> getProductDetails(@PathVariable Long id) {
        // TODO: Wywołanie productService.getProductDetails(id)
        return ResponseEntity.ok(new ProductDetailsDTO());
    }
}
