package pl.edu.pwr.pizzeria.pizzeriabackend.model.enums;

import java.util.Arrays;

public enum OrderItemStatus {
    PENDING("pending"),
    PREPARING("preparing"),
    READY("ready"),
    CANCELLED("cancelled");

    private final String dbValue;

    OrderItemStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static OrderItemStatus fromString(String text) {
        return Arrays.stream(OrderItemStatus.values())
                .filter(status -> status.dbValue.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OrderItemStatus constant with text " + text + " found"));
    }
}

