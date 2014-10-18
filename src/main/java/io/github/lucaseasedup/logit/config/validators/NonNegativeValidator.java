package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;

public final class NonNegativeValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).intValue() >= 0;
        }
        
        return false;
    }
}
