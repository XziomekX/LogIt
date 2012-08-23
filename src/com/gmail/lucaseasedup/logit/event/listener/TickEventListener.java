/*
 * TickEventListener.java
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
import com.gmail.lucaseasedup.logit.event.TickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author LucasEasedUp
 */
public class TickEventListener implements Listener
{
    public TickEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onTick(TickEvent event)
    {
        if (!core.getConfig().getForceLoginPreventAirDepletion())
            return;
        
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        
        for (Player player : players)
        {
            if (!core.getSessionManager().isSessionAlive(player))
            {
                player.setRemainingAir(player.getMaximumAir());
            }
        }
    }
    
    private LogItCore core;
}
