package io.github.lucaseasedup.logit.test;

public final class SelfTestException extends Exception
{
    public SelfTestException()
    {
    }
    
    public SelfTestException(String message)
    {
        super(message);
    }
    
    public SelfTestException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public SelfTestException(Throwable cause)
    {
        super(cause);
    }
}
