package com.gmail.lucaseasedup.logit.event;

import com.gmail.lucaseasedup.logit.LogItCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author LucasEasedUp
 */
public class TickEventListener implements Listener
{
    public TickEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onTick(TickEvent event)
    {
        if (!core.getConfig().getOutOfSessionEventPreventionAirDepletion())
            return;
        
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        
        for (Player player : players)
        {
            if (!core.getSessionManager().isSessionAlive(player))
            {
                player.setRemainingAir(player.getMaximumAir());
            }
        }
    }
    
    private LogItCore core;
}
