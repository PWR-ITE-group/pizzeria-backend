package pl.edu.pwr.pizzeria.pizzeriabackend.model.enums;

public enum MovementType {
    USE("use"),
    RESTOCK("restock"),
    ADJUSTMENT("adjustment");

    private final String value;

    MovementType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MovementType fromString(String value) {
        for (MovementType type : MovementType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid movement type: " + value + 
                ". Must be one of: use, restock, adjustment");
    }
}

