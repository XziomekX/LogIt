package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.common.CancellableEvent;
import org.bukkit.event.HandlerList;

public final class LogItCoreStartEvent extends CancellableEvent
{
    /* package */ LogItCoreStartEvent(LogItCore core)
    {
        if (core == null)
            throw new IllegalArgumentException();
        
        this.core = core;
    }
    
    public LogItCore getCore()
    {
        return core;
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
    
    private final LogItCore core;
}
