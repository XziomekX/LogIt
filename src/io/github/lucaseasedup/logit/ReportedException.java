package io.github.lucaseasedup.logit;

/**
 * Exception that does not need any further logging.
 * 
 * @author LucasEasedUp
 */
public class ReportedException extends Exception
{
    /**
     * Creates a new instance of
     * <code>ReportedException</code> without detail message.
     */
    public ReportedException()
    {
    }
    
    /**
     * Constructs an instance of
     * <code>ReportedException</code> with the specified detail message.
     * <p/>
     * @param msg the detail message.
     */
    public ReportedException(String msg)
    {
        super(msg);
    }
    
    public ReportedException(Throwable cause)
    {
        super(cause);
    }
}
