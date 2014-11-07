package io.github.lucaseasedup.logit.logging.timing;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.bukkit.configuration.file.YamlConfiguration;

public final class TakeoffTiming extends Timing
{
    public void startEvent()
    {
        if (preEvent > 0)
            throw new IllegalStateException();
        
        preEvent = getCurrentTimestamp();
    }

    public void endEvent()
    {
        if (postEvent > 0)
            throw new IllegalStateException();
        
        postEvent = getCurrentTimestamp();
    }

    public void startConfigurationManager()
    {
        if (preConfigurationManager > 0)
            throw new IllegalStateException();
        
        preConfigurationManager = getCurrentTimestamp();
    }

    public void endConfigurationManager()
    {
        if (postConfigurationManager > 0)
            throw new IllegalStateException();
        
        postConfigurationManager = getCurrentTimestamp();
    }
    
    public void startLogger()
    {
        if (preLogger > 0)
            throw new IllegalStateException();
        
        preLogger = getCurrentTimestamp();
    }
    
    public void endLogger()
    {
        if (postLogger > 0)
            throw new IllegalStateException();
        
        postLogger = getCurrentTimestamp();
    }
    
    public void startMessages()
    {
        if (preMessages > 0)
            throw new IllegalStateException();
        
        preMessages = getCurrentTimestamp();
    }

    public void endMessages()
    {
        if (postMessages > 0)
            throw new IllegalStateException();
        
        postMessages = getCurrentTimestamp();
    }
    
    public void startCraftReflect()
    {
        if (preCraftReflect > 0)
            throw new IllegalStateException();
        
        preCraftReflect = getCurrentTimestamp();
    }
    
    public void endCraftReflect()
    {
        if (postCraftReflect > 0)
            throw new IllegalStateException();
        
        postCraftReflect = getCurrentTimestamp();
    }
    
    public void startAccountManager()
    {
        if (preAccountManager > 0)
            throw new IllegalStateException();
        
        preAccountManager = getCurrentTimestamp();
    }
    
    public void endAccountManager()
    {
        if (postAccountManager > 0)
            throw new IllegalStateException();
        
        postAccountManager = getCurrentTimestamp();
    }
    
    public void startPersistenceManager()
    {
        if (prePersistenceManager > 0)
            throw new IllegalStateException();
        
        prePersistenceManager = getCurrentTimestamp();
    }

    public void endPersistenceManager()
    {
        if (postPersistenceManager > 0)
            throw new IllegalStateException();
        
        postPersistenceManager = getCurrentTimestamp();
    }
    
    @Override
    public void saveTiming(File reportFile) throws IOException
    {
        if (reportFile == null)
            throw new IllegalArgumentException();
        
        YamlConfiguration timings =
                YamlConfiguration.loadConfiguration(reportFile);
        
        timings.set("lastTakeoff.timestamp", new Date().toString());
        timings.set("lastTakeoff.total", end - start);
        timings.set("lastTakeoff.event", postEvent - preEvent);
        timings.set("lastTakeoff.configurationManager", postConfigurationManager - preConfigurationManager);
        timings.set("lastTakeoff.logger", postLogger - preLogger);
        timings.set("lastTakeoff.messages", postMessages - preMessages);
        timings.set("lastTakeoff.craftReflect", postCraftReflect - preCraftReflect);
        timings.set("lastTakeoff.accountManager", postAccountManager - preAccountManager);
        timings.set("lastTakeoff.persistenceManager", postPersistenceManager - prePersistenceManager);
        
        timings.save(reportFile);
    }
    
    private long preEvent = -1;
    private long postEvent = -2;
    
    private long preConfigurationManager = -1;
    private long postConfigurationManager = -2;
    
    private long preLogger = -1;
    private long postLogger = -2;
    
    private long preMessages = -1;
    private long postMessages = -2;
    
    private long preCraftReflect = -1;
    private long postCraftReflect = -2;
    
    private long preAccountManager = -1;
    private long postAccountManager = -2;
    
    private long prePersistenceManager = -1;
    private long postPersistenceManager = -2;
}
