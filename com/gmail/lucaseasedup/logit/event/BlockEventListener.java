package com.gmail.lucaseasedup.logit.event;

import com.gmail.lucaseasedup.logit.LogItCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author LucasEasedUp
 */
public class BlockEventListener implements Listener
{
    public BlockEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionBlockPlace())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendEventPreventionMessage(player);
        }
    }
    
    @EventHandler
    private void onBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionBlockBreak())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendEventPreventionMessage(player);
        }
    }
    
    private LogItCore core;
}
