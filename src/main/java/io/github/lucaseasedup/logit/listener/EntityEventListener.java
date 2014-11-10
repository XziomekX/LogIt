package io.github.lucaseasedup.logit.listener;

import io.github.lucaseasedup.logit.LogItCoreObject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class EntityEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onDamageIn(EntityDamageEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.damageIn")
                || !(event.getEntity() instanceof Player))
        {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onDamageOut(EntityDamageByEntityEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.damageOut")
                || !(event.getDamager() instanceof Player))
        {
            return;
        }
        
        Player player = (Player) event.getDamager();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onRegainHealth(EntityRegainHealthEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.regainHealth")
                || !(event.getEntity() instanceof Player))
        {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.foodLevelChange")
                || !(event.getEntity() instanceof Player))
        {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityTarget(EntityTargetEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.entityTarget")
                || !(event.getTarget() instanceof Player))
        {
            return;
        }
        
        Player player = (Player) event.getTarget();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
}
