package io.github.lucaseasedup.logit.common;

/**
 * Exception that does not need any further logging.
 */
public class ReportedException extends RuntimeException
{
    private ReportedException()
    {
    }
    
    private ReportedException(String msg)
    {
        super(msg);
    }
    
    private ReportedException(Throwable cause)
    {
        super(cause);
    }
    
    private ReportedException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    public void rethrow()
    {
        if (shouldSignal())
        {
            throw this;
        }
    }
    
    public void rethrowAsFatal() throws FatalReportedException
    {
        FatalReportedException.throwNew(getMessage(), getCause());
    }
    
    public static void throwNew()
    {
        if (shouldSignal())
        {
            throw new ReportedException();
        }
    }
    
    public static void throwNew(String msg)
    {
        if (shouldSignal())
        {
            throw new ReportedException(msg);
        }
    }
    
    public static void throwNew(Throwable cause)
    {
        if (shouldSignal())
        {
            throw new ReportedException(cause);
        }
    }
    
    public static void throwNew(String msg, Throwable cause)
    {
        if (shouldSignal())
        {
            throw new ReportedException(msg, cause);
        }
    }
    
    public static void incrementRequestCount()
    {
        RequestCounter counter = requestCounter.get();

        if (counter == null)
        {
            requestCounter.set(counter = new RequestCounter());
        }
        
        counter.count++;
    }
    
    public static void decrementRequestCount()
    {
        RequestCounter counter = requestCounter.get();

        if (counter == null)
        {
            requestCounter.set(counter = new RequestCounter());
        }
        
        counter.count--;
    }
    
    public static boolean shouldSignal()
    {
        RequestCounter counter = requestCounter.get();
        
        if (counter == null)
        {
            requestCounter.set(counter = new RequestCounter());
        }
        
        return counter.count > 0;
    }
    
    private static class RequestCounter
    {
        public int count = 0;
    }
    
    private static final long serialVersionUID = 1L;
    private static final ThreadLocal<RequestCounter> requestCounter = new ThreadLocal<>();
}
