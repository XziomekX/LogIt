package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;
import io.github.lucaseasedup.logit.security.model.AuthMeHashingModel;
import io.github.lucaseasedup.logit.security.model.HashingModel;
import io.github.lucaseasedup.logit.security.model.HashingModelDecoder;

public final class HashingAlgorithmValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null)
            return false;
        
        HashingModel hashingModel =
                HashingModelDecoder.decode(value.toString());
        
        return hashingModel != null
                && !(hashingModel instanceof AuthMeHashingModel);
    }
}
