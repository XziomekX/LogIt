/*
 * ServerEventListener.java
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
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.HIGHEST;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * @author LucasEasedUp
 */
public class ServerEventListener extends EventListener
{
    public ServerEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler
    private void onPluginEnable(PluginEnableEvent event)
    {
        if (!event.getPlugin().equals(core.getPlugin()))
            return;
        
        File sessions = new File(core.getPlugin().getDataFolder() + "/" + core.getConfig().getString("storage.sessions.filename"));
        
        try
        {
            core.getSessionManager().importSessions(sessions);
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ServerEventListener.class.getName()).log(Level.WARNING, null, ex);
        }
        
        sessions.delete();
    }
    
    @EventHandler(priority = HIGHEST)
    private void onPluginDisable(PluginDisableEvent event)
    {
        if (!event.getPlugin().equals(core.getPlugin()))
            return;
        
        File sessions = new File(core.getPlugin().getDataFolder() + "/" + core.getConfig().getString("storage.sessions.filename"));
        
        try
        {
            core.getSessionManager().exportSessions(sessions);
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ServerEventListener.class.getName()).log(Level.WARNING, null, ex);
        }
        
        Player[] players = Bukkit.getOnlinePlayers();
        
        for (Player player : players)
        {
            if (core.getConfig().getBoolean("waiting-room.enabled"))
                core.getWaitingRoom().remove(player);
            
            core.getInventoryDepository().withdraw(player);
        }
    }
}
