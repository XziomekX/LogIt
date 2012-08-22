/*
 * EntityEventListener.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.event.listener;

import com.gmail.lucaseasedup.logit.LogItCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * @author LucasEasedUp
 */
public class EntityEventListener implements Listener
{
    public EntityEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onDamageIn(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        
        if (!core.getConfig().getOutOfSessionEventPreventionDamageIn())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onDamageOut(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player))
            return;
        
        Player player = (Player) event.getDamager();
        
        if (!core.getConfig().getOutOfSessionEventPreventionDamageOut())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onRegainHealth(EntityRegainHealthEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        
        if (!core.getConfig().getOutOfSessionEventPreventionRegainHealth())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        
        if (!core.getConfig().getOutOfSessionEventPreventionFoodLevelChange())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    private LogItCore core;
}
