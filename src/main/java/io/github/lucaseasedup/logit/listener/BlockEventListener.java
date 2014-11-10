package io.github.lucaseasedup.logit.listener;

import io.github.lucaseasedup.logit.LogItCoreObject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public final class BlockEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlace(BlockPlaceEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.blockPlace"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig("config.yml").getBoolean("forceLogin.promptOn.blockPlace"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onBreak(BlockBreakEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.blockBreak"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig("config.yml").getBoolean("forceLogin.promptOn.blockBreak"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onHangingBreak(HangingBreakByEntityEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.blockBreak")
                || !(event.getRemover() instanceof Player))
            return;
        
        Player player = (Player) event.getRemover();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig("config.yml").getBoolean("forceLogin.promptOn.blockBreak"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
}
