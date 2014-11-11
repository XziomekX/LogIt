package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;

public final class DataFileValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null)
            return false;
        
        return LogItCore.getInstance().getDataFile(
                value.toString()
        ).isFile();
    }
}
