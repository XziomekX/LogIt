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
        
        File sessionsFile =
                getDataFile(getConfig("config.yml").getString("storage.sessions.filename"));
        
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
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPluginDisable(PluginDisableEvent event)
    {
        if (!event.getPlugin().equals(getPlugin()) || !getCore().isStarted())
            return;
        
        File sessionsFile =
                getDataFile(getConfig("config.yml").getString("storage.sessions.filename"));
        
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
