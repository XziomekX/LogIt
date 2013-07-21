package io.github.lucaseasedup.logit.inventory;

/**
 * @author LucasEasedUp
 */
public class InventorySerializationException extends Exception
{
    /**
     * Creates a new instance of
     * <code>InventorySerializationException</code> without detail message.
     */
    public InventorySerializationException()
    {
    }
    
    /**
     * Constructs an instance of
     * <code>InventorySerializationException</code> with the specified detail message.
     * <p/>
     * @param msg the detail message.
     */
    public InventorySerializationException(String msg)
    {
        super(msg);
    }
    
    public InventorySerializationException(Throwable cause)
    {
        super(cause);
    }
}
