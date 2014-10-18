package io.github.lucaseasedup.logit.common;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class CancellableEvent extends Event implements Cancellable
{
    @Override
    public final boolean isCancelled()
    {
        return cancelled;
    }
    
    @Override
    public final void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
    
    private boolean cancelled = false;
}
