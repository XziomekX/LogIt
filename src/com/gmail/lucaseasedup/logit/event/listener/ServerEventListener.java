/*
 * ServerEventListener.java
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
import org.bukkit.Bukkit;
import static org.bukkit.event.EventPriority.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * @author LucasEasedUp
 */
public class ServerEventListener implements Listener
{
    public ServerEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onPluginEnable(PluginEnableEvent event)
    {
        if (!event.getPlugin().equals(core.getPlugin()))
            return;
        
        Player[] players = Bukkit.getOnlinePlayers();
        
        for (Player player : players)
        {
            core.getSessionManager().createSession(player.getName());
            
            if (core.isPlayerForcedToLogin(player))
            {
                core.sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onPluginDisable(PluginDisableEvent event)
    {
        if (!event.getPlugin().equals(core.getPlugin()))
            return;
        
        Player[] players = Bukkit.getOnlinePlayers();
        
        for (Player player : players)
        {
            core.getWaitingRoom().remove(player);
            core.getSessionManager().destroySession(player.getName());
        }
    }
    
    private final LogItCore core;
}
