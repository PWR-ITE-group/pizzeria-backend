package pl.edu.pwr.pizzeria.pizzeriabackend.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    public void updateStock(Long ingredientId, double quantityChange) {
        // TODO: Aktualizacja pola stock_quantity w tabeli ingredients.
        // TODO: Zapis ruchu w tabeli inventory_movements.
    }

    public List<Object> getAllIngredientsStock() {
        // TODO: Pobranie listy składników z aktualnym stanem magazynowym
        return null;
    }
}
