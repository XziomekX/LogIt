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
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.session.SessionEndEvent;
import io.github.lucaseasedup.logit.session.SessionStartEvent;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class SessionEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    private void onStart(SessionStartEvent event)
    {
        String username = event.getUsername();
        
        if (event.isCancelled() || !PlayerUtils.isPlayerOnline(username))
            return;
        
        final Player player = PlayerUtils.getPlayer(username);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (getConfig().getBoolean("groups.enabled"))
                {
                    getCore().updatePlayerGroup(player);
                }
                
                if (getCore().isPlayerForcedToLogIn(player))
                {
                    broadcastJoinMessage(player, getConfig().getBoolean("reveal-spawn-world"));
                }
            }
        }, 1L);
        
        if (getCore().isPlayerForcedToLogIn(player))
        {
            getPersistenceManager().unserialize(player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    private void onEnd(SessionEndEvent event)
    {
        String username = event.getUsername();
        
        if (event.isCancelled() || !PlayerUtils.isPlayerOnline(username))
            return;
        
        final Player player = PlayerUtils.getPlayer(username);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (getConfig().getBoolean("groups.enabled"))
                {
                    getCore().updatePlayerGroup(player);
                }
                
                if (getCore().isPlayerForcedToLogIn(player))
                {
                    broadcastQuitMessage(player);
                }
            }
        }, 1L);
        
        if (getCore().isPlayerForcedToLogIn(player))
        {
            getPersistenceManager().serialize(player);
        }
    }
}
