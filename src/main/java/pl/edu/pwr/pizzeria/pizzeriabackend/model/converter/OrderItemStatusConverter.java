package pl.edu.pwr.pizzeria.pizzeriabackend.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.OrderItemStatus;

@Converter(autoApply = true)
public class OrderItemStatusConverter implements AttributeConverter<OrderItemStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderItemStatus orderItemStatus) {
        if (orderItemStatus == null) {
            return null;
        }
        return orderItemStatus.getDbValue();
    }

    @Override
    public OrderItemStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return OrderItemStatus.fromString(dbValue);
    }
}

