package io.github.lucaseasedup.logit.test;

import io.github.lucaseasedup.logit.LogItCoreObject;

public final class SelfTestBootstrap extends LogItCoreObject
{
    public void run() throws SelfTestException
    {
        try
        {
            new SelfTestA().run();
            new SelfTestB().run();
        }
        catch (SelfTestException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new SelfTestException(ex);
        }
    }
}
