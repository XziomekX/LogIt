package io.github.lucaseasedup.logit.session;

import org.bukkit.event.HandlerList;

public final class SessionDestroyEvent extends SessionEvent
{
    /* package */ SessionDestroyEvent(String username, Session session)
    {
        super(username, session);
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
