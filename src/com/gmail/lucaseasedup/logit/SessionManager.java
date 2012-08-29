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
import com.gmail.lucaseasedup.logit.event.session.SessionCreateEvent;
import com.gmail.lucaseasedup.logit.event.session.SessionDestroyEvent;
import com.gmail.lucaseasedup.logit.event.session.SessionEndEvent;
import com.gmail.lucaseasedup.logit.event.session.SessionStartEvent;
import java.util.HashMap;
import java.util.Iterator;
import static java.util.logging.Level.FINE;
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
        long forceLoginTimeoutTicks = (core.getConfig().getForceLoginTimeout() > 0L) ? (core.getConfig().getForceLoginTimeout() * -20L) : 0L;
        
        for (Iterator<String> it = sessions.keySet().iterator(); it.hasNext();)
        {
            String  username = it.next();
            Session session = sessions.get(username);
            Player  player = getPlayer(username);
            
            if (session.getStatus() >= 0L)
            {
                if (session.getStatus() > (core.getConfig().getSessionLifetime() * 20L))
                {
                    if (isPlayerOnline(username))
                    {
                        session.setStatus(0L);
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
            else if (isPlayerOnline(username))
            {
                if (session.getStatus() <= forceLoginTimeoutTicks && forceLoginTimeoutTicks > 0L
                        && core.isPlayerForcedToLogin(username) && !player.hasPermission("logit.force-login.timeout.exempt"))
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
    
    /**
     * Checks if the session of a player with the specified player is alive.
     * 
     * @param username Username.
     * @return True, if alive.
     */
    public boolean isSessionAlive(String username)
    {
        Session session = getSession(username);
        
        return (session != null) ? session.isAlive() : false;
    }
    
    /**
     * Checks if the session of the specified player is alive.
     * 
     * @param player Player.
     * @return True, if alive.
     */
    public boolean isSessionAlive(Player player)
    {
        return isSessionAlive(player.getName());
    }
    
    /**
     * Creates a session for a player with the specified username.
     * 
     * @param username Username.
     */
    public void createSession(String username)
    {
        Session session = new Session();
        sessions.put(username.toLowerCase(), session);
        
        // Notify about the session creation.
        core.log(FINE, getMessage("SESSION_CREATE_SUCCESS").replace("%player%", username));
        
        // Call the appropriate event.
        core.callEvent(new SessionCreateEvent(username, session));
    }
    
    /**
     * Creates a session for the specified player.
     * 
     * @param player Player
     */
    public void createSession(Player player)
    {
        createSession(player.getName());
    }
    
    /**
     * Destroys session belonging to a player with the specified username.
     * 
     * @param username Username.
     */
    public void destroySession(String username)
    {
        Session session = sessions.remove(username.toLowerCase());
        
        // Notify about the session destruction.
        core.log(FINE, getMessage("SESSION_DESTROY_SUCCESS").replace("%player%", username));
        
        // Call the appropriate event.
        core.callEvent(new SessionDestroyEvent(username, session));
    }
    
    /**
     * Destroys session belonging to the specified player.
     * 
     * @param player Player.
     */
    public void destroySession(Player player)
    {
        destroySession(player.getName());
    }
    
    /**
     * Starts the session of a player with the specified username.
     * 
     * @param username Username.
     */
    public void startSession(String username)
    {
        Session session = getSession(username);
        session.setStatus(0L);
        
        // Notify about the session start.
        core.log(FINE, getMessage("START_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        // Call the appropriate event.
        core.callEvent(new SessionStartEvent(username, session));
    }
    
    /**
     * Starts the session of the specified player.
     * 
     * @param player Player.
     */
    public void startSession(Player player)
    {
        startSession(player.getName());
    }
    
    /**
     * Ends the session of a player with the specified username.
     * 
     * @param username Username.
     */
    public void endSession(String username)
    {
        Session session = getSession(username);
        session.setStatus(-1L);
        
        // Notify about the session end.
        core.log(FINE, getMessage("END_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        // Call the appropriate event.
        core.callEvent(new SessionEndEvent(username, session));
    }
    
    /**
     * Ends the session of the specified player.
     * 
     * @param player Player.
     */
    public void endSession(Player player)
    {
        endSession(player.getName());
    }
    
    private final LogItCore core;
    
    private final HashMap<String, Session> sessions = new HashMap<>();
}
