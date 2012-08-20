package com.gmail.lucaseasedup.logit;

import com.gmail.lucaseasedup.logit.event.TickEvent;
import org.bukkit.Bukkit;

/**
 * @author LucasEasedUp
 */
public class TickEventCaller implements Runnable
{
    public TickEventCaller()
    {
    }
    
    @Override
    public void run()
    {
        Bukkit.getServer().getPluginManager().callEvent(new TickEvent());
    }
}
