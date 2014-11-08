package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.LogItCore;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;

public final class StoragePinger extends BukkitRunnable
{
    public StoragePinger(Storage storage)
    {
        if (storage == null)
            throw new IllegalArgumentException();
        
        this.storage = storage;
    }
    
    @Override
    public void run()
    {
        try
        {
            storage.ping();
        }
        catch (IOException ex)
        {
            LogItCore.getInstance().log(Level.WARNING,
                    "Could not ping the storage", ex);
        }
    }
    
    private final Storage storage;
}
