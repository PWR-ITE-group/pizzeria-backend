package pl.edu.pwr.pizzeria.pizzeriabackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientOrderRequest {
    private OrderType orderType;                    // тип заказа (DINE_IN, TAKEAWAY, DELIVERY)
    private List<AddItemRequest> items;              // список товаров для заказа
    private String paymentMethod;                   // метод оплаты: "cash", "card", "online" (optional)
    private String customerEmail;                   // email клиента (optional, для отправки ссылки)
    private String customerPhone;                   // телефон клиента (optional)
}

