package pl.edu.pwr.pizzeria.pizzeriabackend.model.enums;

import java.util.Arrays;

public enum OrderType {
    DELIVERY("delivery"),
    PICKUP("pickup"),
    DINE_IN("dine_in");

    private final String dbValue;

    OrderType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static OrderType fromString(String text) {
        return Arrays.stream(OrderType.values())
                .filter(type -> type.dbValue.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OrderType constant with text " + text + " found"));
    }
}

