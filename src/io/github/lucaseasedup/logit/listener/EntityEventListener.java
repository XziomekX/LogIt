/*
 * EntityEventListener.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.listener;

import io.github.lucaseasedup.logit.LogItCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * @author LucasEasedUp
 */
public class EntityEventListener extends EventListener
{
    public EntityEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler
    private void onDamageIn(EntityDamageEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.damage-in") || !(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onDamageOut(EntityDamageByEntityEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.damage-out") || !(event.getDamager() instanceof Player))
            return;
        
        Player player = (Player) event.getDamager();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onRegainHealth(EntityRegainHealthEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.regain-health") || !(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.food-level-change") || !(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onEntityTarget(EntityTargetEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.entity-target") || !(event.getTarget() instanceof Player))
            return;
        
        Player player = (Player) event.getTarget();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
}
