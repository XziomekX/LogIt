package io.github.lucaseasedup.logit.logging.timing;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PlayerLoginTiming extends Timing
{
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
    
    @Override
    public void saveTiming(File reportFile) throws IOException
    {
        if (reportFile == null)
            throw new IllegalArgumentException();
        
        YamlConfiguration timings =
                YamlConfiguration.loadConfiguration(reportFile);
        
        timings.set("lastPlayerLogin.timestamp", new Date().toString());
        timings.set("lastPlayerLogin.total", end - start);
        timings.set("lastPlayerLogin.selectAccount", postSelectAccount - preSelectAccount);
        
        timings.save(reportFile);
    }
    
    private long preSelectAccount = -1;
    private long postSelectAccount = -2;
}
