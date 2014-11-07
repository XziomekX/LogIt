package io.github.lucaseasedup.logit.logging.timing;

import java.io.File;
import java.io.IOException;

public abstract class Timing
{
    public abstract void saveTiming(File reportFile) throws IOException;
    
    public final void start()
    {
        if (start > 0)
            throw new IllegalStateException();
        
        start = getCurrentTimestamp();
    }
    
    public final void end()
    {
        if (end > 0)
            throw new IllegalStateException();
        
        end = getCurrentTimestamp();
    }
    
    protected final long getCurrentTimestamp()
    {
        return System.currentTimeMillis();
    }
    
    protected long start = -1;
    protected long end = -1;
}
