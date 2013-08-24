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

import static org.bukkit.event.EventPriority.HIGHEST;
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * @author LucasEasedUp
 */
public final class ServerEventListener extends LogItCoreObject implements Listener
{
    public ServerEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler
    private void onPluginEnable(PluginEnableEvent event)
    {
        if (!event.getPlugin().equals(getPlugin()))
            return;
        
        File sessions = new File(getDataFolder(), getConfig().getString("storage.sessions.filename"));
        
        try
        {
            getSessionManager().importSessions(sessions);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not import sessions.", ex);
        }
        
        sessions.delete();
    }
    
    @EventHandler(priority = HIGHEST)
    private void onPluginDisable(PluginDisableEvent event)
    {
        if (!event.getPlugin().equals(getPlugin()) || !getCore().isStarted())
            return;
        
        File sessions = new File(getDataFolder(), getConfig().getString("storage.sessions.filename"));
        
        try
        {
            getSessionManager().exportSessions(sessions);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not export sessions.", ex);
        }
    }
}
