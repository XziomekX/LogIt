package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;

public final class CacheTypeValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null)
            return false;
        
        String s = value.toString().toLowerCase();
        
        return s.equals("disabled") || s.equals("preloaded");
    }
}
