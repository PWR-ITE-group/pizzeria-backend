package pl.edu.pwr.pizzeria.pizzeriabackend.model.enums;

import java.util.Arrays;

public enum ModificationType {
    BASE("base"),
    ADDED("added"),
    REMOVED("removed");

    private final String dbValue;

    ModificationType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ModificationType fromString(String text) {
        return Arrays.stream(ModificationType.values())
                .filter(type -> type.dbValue.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ModificationType constant with text " + text + " found"));
    }
}

