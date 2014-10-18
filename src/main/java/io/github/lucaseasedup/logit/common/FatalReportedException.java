package io.github.lucaseasedup.logit.common;

/**
 * Exception that does not need any further logging,
 * and is a signal to stop execution immediately.
 */
public class FatalReportedException extends Exception
{
    private FatalReportedException()
    {
    }
    
    private FatalReportedException(String msg)
    {
        super(msg);
    }
    
    private FatalReportedException(Throwable cause)
    {
        super(cause);
    }
    
    private FatalReportedException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    public void rethrow() throws FatalReportedException
    {
        throw this;
    }
    
    public static void throwNew() throws FatalReportedException
    {
        throw new FatalReportedException();
    }
    
    public static void throwNew(String msg) throws FatalReportedException
    {
        throw new FatalReportedException(msg);
    }
    
    public static void throwNew(Throwable cause) throws FatalReportedException
    {
        throw new FatalReportedException(cause);
    }
    
    public static void throwNew(String msg, Throwable cause) throws FatalReportedException
    {
        throw new FatalReportedException(msg, cause);
    }
    
    private static final long serialVersionUID = 1L;
}
