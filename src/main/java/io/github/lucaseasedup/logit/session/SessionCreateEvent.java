package io.github.lucaseasedup.logit.session;

import org.bukkit.event.HandlerList;

public final class SessionCreateEvent extends SessionEvent
{
    /* package */ SessionCreateEvent(String username)
    {
        super(username, null);
    }
    
    /**
     * Always returns <code>null</code>
     */
    @Override
    public Session getSession()
    {
        return null;
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
}
