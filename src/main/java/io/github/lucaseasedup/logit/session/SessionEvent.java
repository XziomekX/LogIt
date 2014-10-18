package io.github.lucaseasedup.logit.session;

import io.github.lucaseasedup.logit.common.CancellableEvent;
import org.bukkit.event.HandlerList;

public abstract class SessionEvent extends CancellableEvent
{
    public SessionEvent(String username, Session session)
    {
        this.username = username;
        this.session = session;
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public Session getSession()
    {
        return session;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    private final String username;
    private final Session session;
}
