package pl.edu.pwr.pizzeria.pizzeriabackend.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderStatus;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return orderStatus.getDbValue();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return OrderStatus.fromString(dbValue);
    }
}

