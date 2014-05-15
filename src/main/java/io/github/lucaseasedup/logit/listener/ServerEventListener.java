/*
 * ServerEventListener.java
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
package io.github.lucaseasedup.logit.listener;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public final class ServerEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPluginEnable(PluginEnableEvent event)
    {
        if (!event.getPlugin().equals(getPlugin()))
            return;
        
        File sessionsFile = getDataFile(getConfig().getString("storage.sessions.filename"));
        
        if (sessionsFile.exists())
        {
            try
            {
                getSessionManager().importSessions(sessionsFile);
            }
            catch (IOException ex)
            {
                log(Level.WARNING, "Could not import sessions.", ex);
            }
            
            sessionsFile.delete();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPluginDisable(PluginDisableEvent event)
    {
        if (!event.getPlugin().equals(getPlugin()) || !getCore().isStarted())
            return;
        
        File sessionsFile = getDataFile(getConfig().getString("storage.sessions.filename"));
        
        try
        {
            getSessionManager().exportSessions(sessionsFile);
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not export sessions.", ex);
        }
    }
}
