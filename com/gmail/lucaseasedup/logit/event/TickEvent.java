package com.gmail.lucaseasedup.logit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author LucasEasedUp
 */
public class TickEvent extends Event
{
    public TickEvent()
    {
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
