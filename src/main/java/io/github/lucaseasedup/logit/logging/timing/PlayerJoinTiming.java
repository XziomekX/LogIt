package io.github.lucaseasedup.logit.logging.timing;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PlayerJoinTiming extends Timing
{
    public void startCreateSession()
    {
        if (preCreateSession > 0)
            throw new IllegalStateException();
        
        preCreateSession = getCurrentTimestamp();
    }
    
    public void endCreateSession()
    {
        if (postCreateSession > 0)
            throw new IllegalStateException();
        
        postCreateSession = getCurrentTimestamp();
    }
    
    public void startSelectAccount()
    {
        if (preSelectAccount > 0)
            throw new IllegalStateException();
        
        preSelectAccount = getCurrentTimestamp();
    }
    
    public void endSelectAccount()
    {
        if (postSelectAccount > 0)
            throw new IllegalStateException();
        
        postSelectAccount = getCurrentTimestamp();
    }
    
    public void startUuidMatching()
    {
        if (preUuidMatching > 0)
            throw new IllegalStateException();
        
        preUuidMatching = getCurrentTimestamp();
    }
    
    public void endUuidMatching()
    {
        if (postUuidMatching > 0)
            throw new IllegalStateException();
        
        postUuidMatching = getCurrentTimestamp();
    }
    
    public void startSerialize()
    {
        if (preSerialize > 0)
            throw new IllegalStateException();
        
        preSerialize = getCurrentTimestamp();
    }
    
    public void endSerialize()
    {
        if (postSerialize > 0)
            throw new IllegalStateException();
        
        postSerialize = getCurrentTimestamp();
    }
    
    public void startSafeLocation()
    {
        if (preSafeLocation > 0)
            throw new IllegalStateException();
        
        preSafeLocation = getCurrentTimestamp();
    }
    
    public void endSafeLocation()
    {
        if (postSafeLocation > 0)
            throw new IllegalStateException();
        
        postSafeLocation = getCurrentTimestamp();
    }
    
    @Override
    public void saveTiming(File reportFile) throws IOException
    {
        if (reportFile == null)
            throw new IllegalArgumentException();
        
        YamlConfiguration timings =
                YamlConfiguration.loadConfiguration(reportFile);
        
        timings.set("lastPlayerJoin.timestamp", new Date().toString());
        timings.set("lastPlayerJoin.total", end - start);
        timings.set("lastPlayerJoin.createSession", postCreateSession - preCreateSession);
        timings.set("lastPlayerJoin.selectAccount", postSelectAccount - preSelectAccount);
        timings.set("lastPlayerJoin.uuidMatching", postUuidMatching - preUuidMatching);
        timings.set("lastPlayerJoin.safeLocation", postSafeLocation - preSafeLocation);
        
        timings.save(reportFile);
    }
    
    private long preCreateSession = -1;
    private long postCreateSession = -2;
    
    private long preSelectAccount = -1;
    private long postSelectAccount = -2;
    
    private long preUuidMatching = -1;
    private long postUuidMatching = -2;
    
    private long preSerialize = -1;
    private long postSerialize = -2;
    
    private long preSafeLocation = -1;
    private long postSafeLocation = -2;
}
