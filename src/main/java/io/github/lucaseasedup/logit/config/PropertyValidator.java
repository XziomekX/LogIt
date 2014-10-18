package io.github.lucaseasedup.logit.config;

public interface PropertyValidator
{
    public boolean validate(String path, PropertyType type, Object value);
}
