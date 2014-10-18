package io.github.lucaseasedup.logit.config;

public final class InvalidPropertyValueException extends RuntimeException
{
    public InvalidPropertyValueException(String propertyPath)
    {
        super("Invalid value for the property \"" + propertyPath + "\".");
        
        this.propertyPath = propertyPath;
    }
    
    public String getPropertyPath()
    {
        return propertyPath;
    }
    
    private static final long serialVersionUID = 1L;
    
    private final String propertyPath;
}
