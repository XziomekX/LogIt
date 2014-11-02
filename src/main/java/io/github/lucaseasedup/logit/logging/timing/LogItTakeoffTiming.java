package io.github.lucaseasedup.logit.logging.timing;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LogItTakeoffTiming
{
    public void setStart()
    {
        if (start > 0)
            throw new IllegalStateException();
        
        start = getTime();
    }
    
    public void setPreEvent()
    {
        if (preEvent > 0)
            throw new IllegalStateException();
        
        preEvent = getTime();
    }

    public void setPostEvent()
    {
        if (postEvent > 0)
            throw new IllegalStateException();
        
        postEvent = getTime();
    }

    public void setPreConfman()
    {
        if (preConfman > 0)
            throw new IllegalStateException();
        
        preConfman = getTime();
    }

    public void setPostConfman()
    {
        if (postConfman > 0)
            throw new IllegalStateException();
        
        postConfman = getTime();
    }

    public void setPreMsgs()
    {
        if (preMsgs > 0)
            throw new IllegalStateException();
        
        preMsgs = getTime();
    }

    public void setPostMsgs()
    {
        if (postMsgs > 0)
            throw new IllegalStateException();
        
        postMsgs = getTime();
    }

    public void setPreAccman()
    {
        if (preAccman > 0)
            throw new IllegalStateException();
        
        preAccman = getTime();
    }

    public void setPostAccman()
    {
        if (postAccman > 0)
            throw new IllegalStateException();
        
        postAccman = getTime();
    }

    public void setPrePersman()
    {
        if (prePersman > 0)
            throw new IllegalStateException();
        
        prePersman = getTime();
    }

    public void setPostPersman()
    {
        if (postPersman > 0)
            throw new IllegalStateException();
        
        postPersman = getTime();
    }
    
    public void setEnd()
    {
        if (end > 0)
            throw new IllegalStateException();
        
        end = getTime();
    }
    
    private long getTime()
    {
        return System.currentTimeMillis();
    }
    
    public void saveTiming(File reportFile) throws IOException
    {
        if (reportFile == null)
            throw new IllegalArgumentException();
        
        YamlConfiguration timings = new YamlConfiguration();
        
        timings.set("lastTakeoff.timestamp", new Date().toString());
        timings.set("lastTakeoff.total", end - start);
        timings.set("lastTakeoff.event", postEvent - preEvent);
        timings.set("lastTakeoff.confman", postConfman - preConfman);
        timings.set("lastTakeoff.messages", postMsgs - preMsgs);
        timings.set("lastTakeoff.accman", postAccman - preAccman);
        timings.set("lastTakeoff.persman", postPersman - prePersman);
        
        timings.save(reportFile);
    }
    
    private long start = -1;
    
    private long preEvent = -1;
    private long postEvent = -1;
    
    private long preConfman = -1;
    private long postConfman = -1;
    
    private long preMsgs = -1;
    private long postMsgs = -1;
    
    private long preAccman = -1;
    private long postAccman = -1;
    
    private long prePersman = -1;
    private long postPersman = -1;
    
    private long end = -1;
}
