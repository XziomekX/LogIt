package io.github.lucaseasedup.logit.test;

public final class SelfTestA extends SelfTest
{
    @Override
    public void run() throws Exception
    {
        if (!getCore().isStarted())
        {
            getCore().start();
        }
        
        getCore().restart();
        getCore().stop();
        
        try
        {
            getCore().stop();

            assertTrue(false);
        }
        catch (IllegalStateException ex)
        {
            // Should throw.
        }
        
        getCore().start();
        
        try
        {
            getCore().start();

            assertTrue(false);
        }
        catch (IllegalStateException ex)
        {
            // Should throw.
        }
    }
}
