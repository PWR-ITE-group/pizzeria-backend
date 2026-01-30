package pl.edu.pwr.pizzeria.pizzeriabackend.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderType;

@Converter(autoApply = true)
public class OrderTypeConverter implements AttributeConverter<OrderType, String> {

    @Override
    public String convertToDatabaseColumn(OrderType orderType) {
        if (orderType == null) {
            return null;
        }
        return orderType.getDbValue();
    }

    @Override
    public OrderType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return OrderType.fromString(dbValue);
    }
}

