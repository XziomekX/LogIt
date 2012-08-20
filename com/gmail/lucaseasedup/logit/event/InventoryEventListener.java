package com.gmail.lucaseasedup.logit.event;

import com.gmail.lucaseasedup.logit.LogItCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author LucasEasedUp
 */
public class InventoryEventListener implements Listener
{
    public InventoryEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        
        Player player = (Player) event.getWhoClicked();
        
        if (!core.getConfig().getOutOfSessionEventPreventionInventoryClick())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    private LogItCore core;
}
