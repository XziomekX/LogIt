package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;
import io.github.lucaseasedup.logit.security.HashingAlgorithm;

public final class HashingAlgorithmValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null)
            return false;
        
        HashingAlgorithm algorithmType = HashingAlgorithm.decode(value.toString());
        
        return algorithmType != null && algorithmType != HashingAlgorithm.AUTHME;
    }
}
