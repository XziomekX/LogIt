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
package com.gmail.lucaseasedup.logit.session;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import static com.gmail.lucaseasedup.logit.util.MessageSender.*;
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
        long forceLoginTimeout = (core.getConfig().getForceLoginTimeout() > 0L) ? (-core.getConfig().getForceLoginTimeout()) : Long.MIN_VALUE;
        
        for (Iterator<String> it = sessions.keySet().iterator(); it.hasNext();)
        {
            String  username = it.next();
            Player  player   = getPlayer(username);
            Session session  = sessions.get(username);
            
            if (session.getStatus() >= 0L)
            {
                if (session.getStatus() > core.getConfig().getSessionLifetime())
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
                if (session.getStatus() <= forceLoginTimeout && core.isPlayerForcedToLogin(player)
                        && !player.hasPermission("logit.force-login.timeout.exempt"))
                {
                    player.kickPlayer(getMessage("FORCE_LOGIN_TIMEOUT"));
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
    
    /**
     * Returns a session attached to the specified username.
     */
    public Session getSession(String username)
    {
        return sessions.get(username.toLowerCase());
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
     * If session already exists, it will be ignored and overridden.
     * 
     * @param username Username.
     */
    public void createSession(String username, String ip)
    {
        // Create session.
        Session session = new Session(ip);
        sessions.put(username.toLowerCase(), session);
        
        // Notify about the session creation.
        core.log(FINE, getMessage("CREATE_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        // Call the appropriate event.
        callEvent(new SessionCreateEvent(username, session));
    }
    
    /**
     * Destroys session belonging to a player with the specified username.
     * 
     * If session does not exist, no action will be taken.
     * 
     * @param username Username.
     */
    public void destroySession(String username)
    {
        if (getSession(username) == null)
            return;
        
        // Destroy session.
        Session session = sessions.remove(username.toLowerCase());
        
        // Notify about the session destruction.
        core.log(FINE, getMessage("DESTROY_SESSION_SUCCESS_LOG").replace("%player%", getPlayerName(username)));
        
        // Call the appropriate event.
        callEvent(new SessionDestroyEvent(username, session));
    }
    
    /**
     * Starts the session of a player with the specified username.
     * 
     * @param username Username.
     */
    public void startSession(String username)
    {
        if (getSession(username) == null)
            throw new SessionNotFoundException();
        
        // Start session.
        Session session = getSession(username);
        session.setStatus(0L);
        
        // Notify about the session start.
        sendMessage(username, getMessage("START_SESSION_SUCCESS_SELF"));
        core.log(FINE, getMessage("START_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        // Call the appropriate event.
        callEvent(new SessionStartEvent(username, session));
    }
    
    /**
     * Ends the session of a player with the specified username.
     * 
     * @param username Username.
     */
    public void endSession(String username)
    {
        if (getSession(username) == null)
            throw new SessionNotFoundException();
        
        // End session.
        Session session = getSession(username);
        session.setStatus(-1L);
        
        // Notify about the session end.
        sendMessage(username, getMessage("END_SESSION_SUCCESS_SELF"));
        core.log(FINE, getMessage("END_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        // Call the appropriate event.
        callEvent(new SessionEndEvent(username, session));
    }
    
    private final LogItCore core;
    
    private final HashMap<String, Session> sessions = new HashMap<>();
}
