package io.github.lucaseasedup.logit.config;

/**
 * @author LucasEasedUp
 */
public class InvalidPropertyValueException extends RuntimeException
{
    /**
     * Creates a new instance of
     * <code>InvalidPropertyValueException</code> without detail message.
     */
    public InvalidPropertyValueException()
    {
    }
    
    /**
     * Constructs an instance of
     * <code>InvalidPropertyValueException</code> with the specified detail message.
     * <p/>
     * @param msg the detail message.
     */
    public InvalidPropertyValueException(String msg)
    {
        super(msg);
    }
}
