package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.storage.StorageEntry;
import org.bukkit.event.HandlerList;

public final class AccountInsertEvent extends AccountEvent
{
    /* package */ AccountInsertEvent(StorageEntry entry)
    {
        if (entry == null)
            throw new IllegalArgumentException();
        
        this.entry = entry;
    }
    
    public String getDatumValue(String key)
    {
        return entry.get(key);
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    private final StorageEntry entry;
}
