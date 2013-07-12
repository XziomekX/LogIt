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

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.session.SessionEndEvent;
import io.github.lucaseasedup.logit.session.SessionStartEvent;
import static io.github.lucaseasedup.logit.util.MessageUtils.broadcastJoinMessage;
import static io.github.lucaseasedup.logit.util.MessageUtils.broadcastQuitMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.LOWEST;

/**
 * @author LucasEasedUp
 */
public class SessionEventListener extends EventListener
{
    public SessionEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler(priority = LOWEST)
    private void onStart(SessionStartEvent event)
    {
        String username = event.getUsername();
        
        if (isPlayerOnline(username))
        {
            Player player = getPlayer(username);
            
            core.getWaitingRoom().remove(player);
            core.getInventoryDepository().withdraw(player);
            
            if (core.getConfig().getBoolean("groups.enabled"))
                core.updatePlayerGroup(player);
            
            if (core.getConfig().getBoolean("force-login.global") && !player.hasPermission("logit.force-login.exempt"))
            {
                broadcastJoinMessage(player, core.getConfig().getBoolean("reveal-spawn-world"));
            }
        }
    }
    
    @EventHandler(priority = LOWEST)
    private void onEnd(SessionEndEvent event)
    {
        String username = event.getUsername();
        
        if (isPlayerOnline(username))
        {
            Player player = getPlayer(username);
            
            if (core.getConfig().getBoolean("force-login.global"))
            {
                if (core.getConfig().getBoolean("waiting-room.enabled"))
                {
                    core.getWaitingRoom().put(player);
                }
                
                if (!player.hasPermission("logit.force-login.exempt"))
                {
                    broadcastQuitMessage(player);
                }
            }
            
            if (core.getConfig().getBoolean("force-login.hide-inventory"))
            {
                core.getInventoryDepository().deposit(player);
            }
            
            if (core.getConfig().getBoolean("groups.enabled"))
            {
                core.updatePlayerGroup(player);
            }
        }
    }
}
