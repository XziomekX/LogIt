package io.github.lucaseasedup.logit;

/**
 * Exception that does not need any further logging,
 * and is a signal to stop execution immediately.
 * 
 * @author LucasEasedUp
 */
public class FatalReportedException extends ReportedException
{
    /**
     * Creates a new instance of
     * <code>FatalReportedException</code> without detail message.
     */
    public FatalReportedException()
    {
    }
    
    /**
     * Constructs an instance of
     * <code>FatalReportedException</code> with the specified detail message.
     * <p/>
     * @param msg the detail message.
     */
    public FatalReportedException(String msg)
    {
        super(msg);
    }
    
    public FatalReportedException(Throwable cause)
    {
        super(cause);
    }
}
