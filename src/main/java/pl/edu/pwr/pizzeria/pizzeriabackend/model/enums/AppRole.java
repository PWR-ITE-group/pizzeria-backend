package pl.edu.pwr.pizzeria.pizzeriabackend.model.enums;

public enum AppRole {
    MANAGER("manager"),
    CHEF("chef"),
    WAITER("waiter"),
    COURIER("courier"),
    CLIENT("client");

    private final String dbRoleName;

    AppRole(String dbRoleName) {
        this.dbRoleName = dbRoleName;
    }

    public String getDbRoleName() {
        return dbRoleName;
    }
}
