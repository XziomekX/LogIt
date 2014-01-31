/*
 * ForcedLoginPrompter.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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
package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ForcedLoginPrompter implements Runnable
{
    public ForcedLoginPrompter(LogItCore core, String username)
    {
        this.core = core;
        this.username = username;
    }
    
    public void setTaskId(int taskId)
    {
        this.taskId = taskId;
    }
    
    @Override
    public void run()
    {
        Player player = PlayerUtils.getPlayer(username);
        
        if (player == null)
        {
            if (taskId != -1)
            {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        else if (core.isPlayerForcedToLogIn(player))
        {
            if (!core.getSessionManager().isSessionAlive(player))
            {
                core.sendForceLoginMessage(player);
            }
            else if (taskId != -1)
            {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }
    
    private final LogItCore core;
    private final String username;
    private int taskId = -1;
}
