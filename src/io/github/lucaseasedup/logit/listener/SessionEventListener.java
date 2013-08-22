/*
 * SessionEventListener.java
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

import static io.github.lucaseasedup.logit.util.MessageUtils.broadcastJoinMessage;
import static io.github.lucaseasedup.logit.util.MessageUtils.broadcastQuitMessage;
import static org.bukkit.event.EventPriority.LOWEST;
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.PlayerHolder;
import io.github.lucaseasedup.logit.inventory.InventorySerializationException;
import io.github.lucaseasedup.logit.session.SessionEndEvent;
import io.github.lucaseasedup.logit.session.SessionStartEvent;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author LucasEasedUp
 */
public class SessionEventListener extends LogItCoreObject implements Listener
{
    public SessionEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler(priority = LOWEST)
    private void onStart(SessionStartEvent event)
    {
        String username = event.getUsername();
        final Player player = PlayerHolder.getExact(username);
        
        try
        {
            getInventoryDepository().withdraw(player);
        }
        catch (InventorySerializationException ex)
        {
            log(Level.WARNING, "Could not withdraw player's inventory.", ex);
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (getConfig().getBoolean("groups.enabled"))
                {
                    getCore().updatePlayerGroup(player);
                }
                
                if (getCore().isPlayerForcedToLogin(player))
                {
                    broadcastJoinMessage(player, getConfig().getBoolean("reveal-spawn-world"));
                }
            }
        }, 1L);
        
        if (getCore().isPlayerForcedToLogin(player))
        {
            getPersistenceManager().unserialize(player, false);
        }
    }
    
    @EventHandler(priority = LOWEST)
    private void onEnd(SessionEndEvent event)
    {
        String username = event.getUsername();
        final Player player = PlayerHolder.getExact(username);
        
        if (getCore().isPlayerForcedToLogin(player))
        {
            if (getConfig().getBoolean("force-login.hide-inventory"))
            {
                try
                {
                    getInventoryDepository().deposit(player);
                }
                catch (InventorySerializationException ex)
                {
                    log(Level.WARNING, "Could not deposit player's inventory.", ex);
                }
            }
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (getConfig().getBoolean("groups.enabled"))
                {
                    getCore().updatePlayerGroup(player);
                }
                
                if (getCore().isPlayerForcedToLogin(player))
                {
                    broadcastQuitMessage(player);
                }
            }
        }, 1L);
        
        if (getCore().isPlayerForcedToLogin(player))
        {
            getPersistenceManager().serialize(player, false);
        }
    }
}
