package pl.edu.pwr.pizzeria.pizzeriabackend.service;



import org.springframework.stereotype.Service;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderRequestDTO;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.dto.OrderStatusDTO;

import java.util.List;

@Service
public class OrderService {

    // --- Publiczne ---

    public OrderStatusDTO placeOrder(OrderRequestDTO request) {
        // TODO: Walidacja, stworzenie encji Order, zapis do DB
        // TODO: Utworzenie pozycji OrderItem i powiązanie ich z Order
        // TODO: Inicjacja płatności (jeśli online)
        return null;
    }

    public OrderStatusDTO getOrderStatusByToken(String token) {
        // TODO: Znalezienie Order w DB po unikalnym tokenie
        return null;
    }

    // --- Pracownicze (WAITER/MANAGER) ---

    public OrderStatusDTO createDineInOrder(OrderRequestDTO request) {
        // TODO: Stworzenie zamówienia z typem DINE_IN i przypisanie do kelnera
        return null;
    }

    public OrderStatusDTO markAsPaid(Long orderId, String paymentMethod) {
        // TODO: Zmiana statusu zamówienia i płatności na PAID
        return null;
    }

    public List<OrderStatusDTO> getCurrentActiveOrders(String role) {
        // TODO: Pobranie aktywnych zamówień, filtrując np. dla kelnera (DINE_IN) lub menadżera (wszystkie)
        return null;
    }
}