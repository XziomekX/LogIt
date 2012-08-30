/*
 * SessionEventListener.java
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
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import static com.gmail.lucaseasedup.logit.MessageSender.*;
import com.gmail.lucaseasedup.logit.SpawnWorldInfoGenerator;
import com.gmail.lucaseasedup.logit.event.SessionEndEvent;
import com.gmail.lucaseasedup.logit.event.SessionStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author LucasEasedUp
 */
public class SessionEventListener implements Listener
{
    public SessionEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onStart(SessionStartEvent event)
    {
        if (isPlayerOnline(event.getUsername()))
        {
            Player player = getPlayer(event.getUsername());
            
            core.getWaitingRoom().remove(player);
            
            if (core.getConfig().getForceLoginGlobal() && !player.hasPermission("logit.login.exempt"))
            {
                broadcastMessage(getMessage("JOIN").replace("%player%", player.getName()) + SpawnWorldInfoGenerator.generate(player), player);
            }
        }
    }
    
    @EventHandler
    private void onEnd(SessionEndEvent event)
    {
        if (isPlayerOnline(event.getUsername()))
        {
            Player player = getPlayer(event.getUsername());
            
            if (core.getConfig().getForceLoginGlobal() && core.getConfig().getWaitingRoomEnabled())
            {
                core.getWaitingRoom().put(player);
            }
            
            if (core.getConfig().getForceLoginGlobal() && !player.hasPermission("logit.login.exempt"))
            {
                broadcastMessage(getMessage("QUIT").replace("%player%", player.getName()), player);
            }
        }
    }
    
    private final LogItCore core;
}
