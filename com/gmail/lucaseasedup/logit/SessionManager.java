package com.gmail.lucaseasedup.logit;

import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class SessionManager implements Runnable
{
    public SessionManager(LogItCore core)
    {
        this.core = core;
    }
    
    @Override
    public void run()
    {
        if (core.getConfig().getOutOfSessionTimeout() < 0L)
            return;
        
        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            String username = player.getName().toLowerCase();
            
            if (core.isPlayerForcedToLogin(player) && !isSessionAlive(player))
            {
                Long l = sessions.get(username);
                
                if (l != null && !player.hasPermission("logit.out-of-session.timeout.exempt") && l < (core.getConfig().getOutOfSessionTimeout() * -20L))
                {
                    player.kickPlayer(getMessage("OUT_OF_SESSION_TIMEOUT", true));
                }
                else
                {
                    sessions.put(username, l - 40L);
                }
            }
        }
    }
    
    public void createSession(String username)
    {
        sessions.put(username.toLowerCase(), -1L);
    }
    
    public void destroySession(String username)
    {
        sessions.remove(username.toLowerCase());
    }
    
    public boolean isSessionAlive(String username)
    {
        if (!sessions.containsKey(username.toLowerCase()))
            return false;
        
        return sessions.get(username.toLowerCase()) >= 0L;
    }
    
    public boolean isSessionAlive(Player player)
    {
        return isSessionAlive(player.getName());
    }
    
    public void startSession(Player player, boolean notify)
    {
        sessions.put(player.getName().toLowerCase(), 0L);
        
        core.takeOutOfWaitingRoom(player);
        
        if (notify)
        {
            if (core.getConfig().getForceLogin() && !player.hasPermission("logit.login.exempt"))
            {
                broadcastMessage(getMessage("JOIN", true).replace("%player%", player.getName()) + SpawnWorldInfoGenerator.getInstance().generate(player));
            }
            else
            {
                player.sendMessage(getMessage("LOGGED_IN_SELF", true));
            }
            
            if (core.getConfig().getVerbose())
            {
                log(Level.INFO, getMessage("LOGGED_IN_OTHERS").replace("%player%", player.getName()));
            }
        }
    }
    
    public void endSession(Player player, boolean notify)
    {
        sessions.put(player.getName().toLowerCase(), -1L);
        
        if (notify)
        {
            if (core.getConfig().getForceLogin() && !player.hasPermission("logit.login.exempt"))
            {
                broadcastMessage(getMessage("QUIT", true).replace("%player%", player.getName()));
            }
            else
            {
                player.sendMessage(getMessage("LOGGED_OUT_SELF", true));
            }
            
            if (core.getConfig().getVerbose())
            {
                log(Level.INFO, getMessage("LOGGED_OUT_OTHERS").replace("%player%", player.getName()));
            }
        }
    }
    
    private LogItCore core;
    
    private HashMap<String, Long> sessions = new HashMap<>();
}
