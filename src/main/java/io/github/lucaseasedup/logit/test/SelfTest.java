package io.github.lucaseasedup.logit.test;

import io.github.lucaseasedup.logit.LogItCoreObject;

public abstract class SelfTest extends LogItCoreObject
{
    public abstract void run() throws Exception;
    
    protected final void assertTrue(boolean condition) throws SelfTestException
    {
        if (!condition)
        {
            throw new SelfTestException("Assertion failed." +
                    " " + condition + " must be true");
        }
    }
    
    protected final void assertFalse(boolean condition) throws SelfTestException
    {
        if (condition)
        {
            throw new SelfTestException("Assertion failed." +
                    " " + condition + " must be false");
        }
    }
}
