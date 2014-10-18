package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;
import io.github.lucaseasedup.logit.storage.StorageType;

public final class StorageTypeValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null)
            return false;
        
        return StorageType.decode(value.toString()) != StorageType.UNKNOWN;
    }
}
