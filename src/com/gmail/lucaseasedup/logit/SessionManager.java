/*
 * SessionManager.java
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
package com.gmail.lucaseasedup.logit;

import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import com.gmail.lucaseasedup.logit.event.SessionCreateEvent;
import com.gmail.lucaseasedup.logit.event.SessionDestroyEvent;
import com.gmail.lucaseasedup.logit.event.SessionEndEvent;
import com.gmail.lucaseasedup.logit.event.SessionStartEvent;
import java.util.HashMap;
import java.util.Map.Entry;
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
        for (Entry<String, Session> entry : sessions.entrySet())
        {
            String username = entry.getKey();
            Session session = entry.getValue();
            
            Player player = getPlayer(username);
            
            if (session.getStatus() >= 0L)
            {
                if (session.getStatus() > (core.getConfig().getSessionLifetime() * 20L))
                {
                    if (isPlayerOnline(username))
                    {
                        session.reset();
                    }
                    else
                    {
                        destroySession(username);
                    }
                }
                else
                {
                    session.updateStatus(20L);
                }
            }
            else if (core.getConfig().getForceLoginTimeout() >= 0L && isPlayerOnline(username) && core.isPlayerForcedToLogin(username))
            {
                if (session.getStatus() < (core.getConfig().getForceLoginTimeout() * -20L) && !player.hasPermission("logit.force-login.timeout.exempt"))
                {
                    player.kickPlayer(getMessage("OUT_OF_SESSION_TIMEOUT"));
                }
                else
                {
                    session.updateStatus(-20L);
                }
            }
            else
            {
                destroySession(username);
            }
        }
    }
    
    public Session getSession(String username)
    {
        return sessions.get(username.toLowerCase());
    }
    
    public Session getSession(Player player)
    {
        return getSession(player.getName());
    }
    
    public void createSession(String username)
    {
        Session session = sessions.put(username.toLowerCase(), new Session());
        
        Bukkit.getServer().getPluginManager().callEvent(new SessionCreateEvent(username, session));
        core.log(Level.FINE, getMessage("SESSION_CREATED").replace("%player%", getPlayerName(username)));
    }
    
    public void createSession(Player player)
    {
        createSession(player.getName());
    }
    
    public void destroySession(String username)
    {
        Session session = sessions.remove(username.toLowerCase());
        
        Bukkit.getServer().getPluginManager().callEvent(new SessionDestroyEvent(username, session));
        core.log(Level.FINE, getMessage("SESSION_DESTROYED").replace("%player%", getPlayerName(username)));
    }
    
    public void destroySession(Player player)
    {
        destroySession(player.getName());
    }
    
    public boolean isSessionAlive(String username)
    {
        Session session = getSession(username);
        
        if (session == null)
        {
            return false;
        }
        
        return session.getStatus() >= 0L;
    }
    
    public boolean isSessionAlive(Player player)
    {
        return isSessionAlive(player.getName());
    }
    
    public void startSession(Player player, boolean notify)
    {
        getSession(player).start();
        
        core.takeOutOfWaitingRoom(player);
        
        if (notify)
        {
            if (core.getConfig().getForceLoginGlobal() && !player.hasPermission("logit.login.exempt"))
            {
                broadcastMessage(getMessage("JOIN").replace("%player%", player.getName()) + SpawnWorldInfoGenerator.getInstance().generate(player));
            }
            else
            {
                player.sendMessage(getMessage("LOGGED_IN_SELF"));
            }
        }
        
        Bukkit.getServer().getPluginManager().callEvent(new SessionStartEvent(player.getName().toLowerCase(), getSession(player)));
        core.log(Level.FINE, getMessage("LOGGED_IN_OTHERS").replace("%player%", player.getName()));
    }
    
    public void endSession(Player player, boolean notify)
    {
        getSession(player).end();
        
        if (core.getConfig().getForceLoginGlobal() && core.getConfig().getWaitingRoomEnabled())
        {
            core.putIntoWaitingRoom(player);
        }
        
        if (notify)
        {
            if (core.getConfig().getForceLoginGlobal() && !player.hasPermission("logit.login.exempt"))
            {
                broadcastMessage(getMessage("QUIT").replace("%player%", player.getName()));
            }
            else
            {
                player.sendMessage(getMessage("LOGGED_OUT_SELF"));
            }
        }
        
        Bukkit.getServer().getPluginManager().callEvent(new SessionEndEvent(player.getName().toLowerCase(), getSession(player)));
        core.log(Level.FINE, getMessage("LOGGED_OUT_OTHERS").replace("%player%", player.getName()));
    }
    
    private final LogItCore core;
    
    private HashMap<String, Session> sessions = new HashMap<>();
}
