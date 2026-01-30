package pl.edu.pwr.pizzeria.pizzeriabackend.model.enums;

import java.util.Arrays;

public enum OrderStatus {
    NEW("new"),
    PREPARING("preparing"),
    READY("ready"),
    DELIVERED("delivered"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    FAILED("failed");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static OrderStatus fromString(String text) {
        return Arrays.stream(OrderStatus.values())
                .filter(status -> status.dbValue.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OrderStatus constant with text " + text + " found"));
    }
}

