package io.github.lucaseasedup.logit.account;

import org.bukkit.event.HandlerList;

public final class AccountRemoveEvent extends AccountEvent
{
    /* package */ AccountRemoveEvent(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        this.username = username;
    }
    
    public String getUsername()
    {
        return username;
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
    
    private final String username;
}
