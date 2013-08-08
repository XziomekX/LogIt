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
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class SessionManager implements Runnable
{
    public SessionManager(LogItCore core, AccountManager accountManager)
    {
        this.core = core;
        this.accountManager = accountManager;
    }
    
    @Override
    public void run()
    {
        long forceLoginTimeout = (core.getConfig().getInt("force-login.timeout") > 0L)
                ? (-core.getConfig().getInt("force-login.timeout") * 20L) : Long.MIN_VALUE;
        
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            String  username = entry.getKey();
            Session session  = entry.getValue();
            Player  player   = getPlayer(username);
            
            if (session.getStatus() >= 0L)
            {
                if (session.getStatus() > (core.getConfig().getInt("session-lifetime") * 20L))
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
                if (core.getAccountManager().isRegistered(username) && !player.hasPermission("logit.force-login.timeout.exempt")
                        && core.isPlayerForcedToLogin(player))
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
            else
            {
                destroySession(username);
            }
        }
    }
    
    /**
     * Returns a session attached to the specified username.
     * 
     * @param username Username.
     * @return Session object.
     */
    public Session getSession(String username)
    {
        return sessions.get(username.toLowerCase());
    }
    
    /**
     * Checks if the session of a player with the specified player is alive.
     * 
     * @param username Username.
     * @return True if alive.
     */
    public boolean isSessionAlive(String username)
    {
        Session session = getSession(username);
        
        return (session != null) ? session.isAlive() : false;
    }
    
    /**
     * Checks if session of the specified player is alive.
     * 
     * @param player Player.
     * @return True if alive.
     */
    public boolean isSessionAlive(Player player)
    {
        return isSessionAlive(player.getName());
    }
    
    /**
     * Creates a session for a player with the specified username.
     * <p/>
     * Providing a valid IP address is important as it prevents session hijacking.
     * <p/>
     * If session already exists, it will be ignored and overridden.
     * 
     * @param username Username.
     * @param ip IP address.
     * @return True if session has been created,
     *         false if creation has been cancelled by an outside event listener.
     */
    public boolean createSession(String username, String ip)
    {
        SessionEvent evt = new SessionCreateEvent(username);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        // Create session.
        Session session = new Session(ip);
        sessions.put(username.toLowerCase(), session);
        
        core.log(Level.FINE, getMessage("CREATE_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return true;
    }
    
    /**
     * Destroys session belonging to a player with the specified username.
     * <p/>
     * If session does not exist, no action will be taken.
     * 
     * @param username Username.
     * @return True if session has been destroyed,
     *         false if operation has been cancelled by an outside event listener.
     */
    public boolean destroySession(String username)
    {
        if (getSession(username) == null)
            return true;
        
        Session session = sessions.get(username.toLowerCase());
        SessionEvent evt = new SessionDestroyEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        sessions.remove(username.toLowerCase());
        
        core.log(Level.FINE, getMessage("DESTROY_SESSION_SUCCESS_LOG").replace("%player%", getPlayerName(username)));
        
        return true;
    }
    
    /**
     * Starts the session of a player with the specified username.
     * 
     * @param username Username.
     * @throws SessionNotFoundException Thrown if the session does not exist.
     * @return True if session has been started,
     *         false if operation has been cancelled by an outside event listener.
     */
    public boolean startSession(String username)
    {
        if (getSession(username) == null)
            throw new SessionNotFoundException();
        
        Session session = getSession(username);
        SessionEvent evt = new SessionStartEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        // Start session.
        session.setStatus(0L);
        
        try
        {
            accountManager.updateLastActiveDate(username);
        }
        catch (SQLException ex)
        {
            core.log(Level.WARNING, "Could not update last-active date.", ex);
        }
        
        core.log(Level.FINE, getMessage("START_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return true;
    }
    
    /**
     * Ends the session of a player with the specified username.
     * 
     * @param username Username.
     * @throws SessionNotFoundException Thrown if the session does not exist.
     * @return True if session has been ended,
     *         false if operation has been cancelled by an outside event listener.
     */
    public boolean endSession(String username)
    {
        if (getSession(username) == null)
            throw new SessionNotFoundException();
        
        Session session = getSession(username);
        SessionEvent evt = new SessionEndEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        // End session.
        session.setStatus(-1L);
        
        try
        {
            accountManager.updateLastActiveDate(username);
        }
        catch (SQLException ex)
        {
            core.log(Level.WARNING, "Could not update last-active date.", ex);
        }
        
        core.log(Level.FINE, getMessage("END_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return true;
    }
    
    public void exportSessions(File sessionsDatabaseFile) throws SQLException
    {
        sessionsDatabaseFile.delete();
        
        try (SqliteDatabase sessionsDatabase = new SqliteDatabase("jdbc:sqlite:" + sessionsDatabaseFile))
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
                    createSession(player.getName(), "");
                
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
    
    private final LogItCore core;
    private final AccountManager accountManager;
    
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
}
