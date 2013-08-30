/*
 * SessionManager.java
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
package io.github.lucaseasedup.logit.session;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerName;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.CancelledState;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.db.Database;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SessionManager extends LogItCoreObject implements Runnable
{
    @Override
    public void run()
    {
        long forceLoginTimeout = (getConfig().getInt("force-login.timeout") > 0L)
                ? (-getConfig().getInt("force-login.timeout") * 20L) : Long.MIN_VALUE;
        
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            String  username = entry.getKey();
            Session session  = entry.getValue();
            Player  player   = getPlayer(username);
            
            // Player logged in.
            if (session.getStatus() >= 0L)
            {
                if (session.getStatus() > (getConfig().getInt("session-lifetime") * 20L))
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
            // Player logged out and online.
            else if (isPlayerOnline(username))
            {
                if (getAccountManager().isRegistered(username)
                        && !player.hasPermission("logit.force-login.timeout.exempt")
                        && getCore().isPlayerForcedToLogIn(player))
                {
                    if (session.getStatus() <= forceLoginTimeout)
                    {
                        player.kickPlayer(getMessage("FORCE_LOGIN_TIMEOUT"));
                    }
                    else
                    {
                        session.updateStatus(-20L);
                    }
                }
            }
            // Player logged out and offline.
            else
            {
                destroySession(username);
            }
        }
    }
    
    /**
     * Returns a session attached to the specified username.
     * 
     * @param username username.
     * @return session object.
     */
    public Session getSession(String username)
    {
        return sessions.get(username.toLowerCase());
    }
    
    /**
     * Checks if a session is alive.
     * 
     * <p> Returns {@code true} if {@code player} is not {@code null}, the session is alive
     * and player IP matches session IP; {@code false} otherwise.
     * 
     * @param player a player which the session is tied to.
     * @return {@code true} if the session is alive; {@code false} otherwise.
     */
    public boolean isSessionAlive(Player player)
    {
        if (player == null)
            return false;
        
        Session session = getSession(player.getName());
        
        if (session == null)
            return false;
        
        String ip = getPlayerIp(player);
        
        return session.isAlive() && ip.equals(session.getIp());
    }
    
    /**
     * Checks if a session is alive.
     * 
     * <p> Returns {@code true} if {@code name} is not {@code null}, the session is alive
     * and, if the player is online, player IP matches session IP; {@code false} otherwise.
     * 
     * @param name player name.
     * @return {@code true} if the session is alive; {@code false} otherwise.
     */
    public boolean isSessionAlive(String name)
    {
        if (name == null)
            return false;
        
        Session session = getSession(name);
        
        if (session == null)
            return false;
        
        if (PlayerUtils.isPlayerOnline(name))
        {
            Player player = getPlayer(name);
            String ip     = getPlayerIp(player);
            
            return session.isAlive() && ip.equals(session.getIp());
        }
        else
        {
            return session.isAlive();
        }
    }
    
    /**
     * Creates a session for a player with the specified username.
     * 
     * <p> If a session with this username already exists,
     * no action will be taken.
     * 
     * @param username the player username.
     * @param ip       the player IP address.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     */
    public CancelledState createSession(String username, String ip)
    {
        if (getSession(username) != null)
            return CancelledState.NOT_CANCELLED;
        
        SessionEvent evt = new SessionCreateEvent(username);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        // Create session.
        Session session = new Session(ip);
        sessions.put(username.toLowerCase(), session);
        
        log(Level.FINE, getMessage("CREATE_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Destroys session belonging to a player with the specified username.
     * 
     * <p> If session does not exist, no action will be taken.
     * 
     * @param username the player username.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     */
    public CancelledState destroySession(String username)
    {
        if (getSession(username) == null)
            return CancelledState.NOT_CANCELLED;
        
        if (isSessionAlive(username))
        {
            endSession(username);
        }
        
        Session session = sessions.get(username.toLowerCase());
        SessionEvent evt = new SessionDestroyEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        sessions.remove(username.toLowerCase());
        
        log(Level.FINE, getMessage("DESTROY_SESSION_SUCCESS_LOG")
                .replace("%player%", getPlayerName(username)));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Starts session of a player with the specified username.
     * 
     * @param username the player username.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws SessionNotFoundException if no such session exists.
     */
    public CancelledState startSession(String username)
    {
        if (getSession(username) == null)
            throw new SessionNotFoundException();
        
        if (isSessionAlive(username))
            return CancelledState.NOT_CANCELLED;
        
        Session session = getSession(username);
        SessionEvent evt = new SessionStartEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        // Start session.
        session.setStatus(0L);
        
        log(Level.FINE, getMessage("START_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Ends session of a player with the specified username.
     * 
     * @param username the player username.
     * .
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     * 
     * @throws SessionNotFoundException if no such session exists.
     */
    public CancelledState endSession(String username)
    {
        if (getSession(username) == null)
            throw new SessionNotFoundException();
        
        if (!isSessionAlive(username))
            return CancelledState.NOT_CANCELLED;
        
        Session session = getSession(username);
        SessionEvent evt = new SessionEndEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        // End session.
        session.setStatus(-1L);
        
        log(Level.FINE, getMessage("END_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    public void exportSessions(File sessionsDatabaseFile) throws SQLException
    {
        sessionsDatabaseFile.delete();
        
        try (Database sessionsDatabase = new SqliteDatabase("jdbc:sqlite:" + sessionsDatabaseFile))
        {
            sessionsDatabase.connect();
            sessionsDatabase.createTableIfNotExists("sessions", new String[]{
                "username", "VARCHAR(16)",
                "status",   "INTEGER",
                "ip",       "VARCHAR(64)"
            });
            
            Player[] players = Bukkit.getOnlinePlayers();
            
            for (Player player : players)
            {
                sessionsDatabase.insert("sessions", new String[]{
                    "username",
                    "status",
                    "ip"
                }, new String[]{
                    player.getName().toLowerCase(),
                    String.valueOf(getSession(player.getName()).getStatus()),
                    getPlayerIp(player)
                });
            }
        }
    }
    
    public void importSessions(File file) throws SQLException
    {
        try (SqliteDatabase sessionsDatabase = new SqliteDatabase("jdbc:sqlite:" + file))
        {
            sessionsDatabase.connect();
            
            Player[] players = Bukkit.getOnlinePlayers();
            
            for (Player player : players)
            {
                if (getSession(player.getName()) == null)
                {
                    createSession(player.getName(), "");
                }
                
                List<Map<String, String>> rs = sessionsDatabase.select("sessions", new String[]{
                    "status",
                    "ip"
                }, new String[]{
                    "username", "=", player.getName().toLowerCase()
                });
                
                if (!rs.isEmpty())
                {
                    Session session = getSession(player.getName());
                    
                    session.setStatus(Integer.parseInt(rs.get(0).get("status")));
                    session.setIp(rs.get(0).get("ip"));
                }
            }
        }
    }
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
}
