package pl.edu.pwr.pizzeria.pizzeriabackend.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.enums.ModificationType;

@Converter(autoApply = true)
public class ModificationTypeConverter implements AttributeConverter<ModificationType, String> {
    @Override
    public String convertToDatabaseColumn(ModificationType modificationType) {
        if (modificationType == null) {
            return null;
        }
        return modificationType.getDbValue();
    }

    @Override
    public ModificationType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return ModificationType.fromString(dbValue);
    }
}

