/*
 * TickEventListener.java
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
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.TickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author LucasEasedUp
 */
public class TickEventListener extends LogItCoreObject implements Listener
{
    public TickEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler
    private void onTick(TickEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.air-depletion"))
            return;
        
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!core.getSessionManager().isSessionAlive(player.getName()))
            {
                player.setRemainingAir(player.getMaximumAir());
            }
        }
    }
}
