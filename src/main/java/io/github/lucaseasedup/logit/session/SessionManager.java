/*
 * SessionManager.java
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
package io.github.lucaseasedup.logit.session;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.CollectionUtils.containsIgnoreCase;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerName;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.CancelledState;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SqliteStorage;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.storage.Storage.Type;
import io.github.lucaseasedup.logit.util.HashtableBuilder;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
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
        
        List<String> disableTimeoutForPlayers =
                getConfig().getStringList("force-login.disable-timeout-for-players");
        
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            String  username = entry.getKey();
            Session session  = entry.getValue();
            Player  player   = getPlayer(username);
            
            // Player logged in.
            if (session.getStatus() >= 0L)
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
            // Player logged out and online.
            else if (isPlayerOnline(username))
            {
                if (!containsIgnoreCase(username, disableTimeoutForPlayers)
                        && getCore().isPlayerForcedToLogIn(player))
                {
                    if (session.getStatus() <= forceLoginTimeout)
                    {
                        player.kickPlayer(getMessage("FORCE_LOGIN_TIMEOUT"));
                    }
                    else
                    {
                        session.updateStatus(-TASK_PERIOD);
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
    
    public void exportSessions(File sessionsDatabaseFile) throws IOException
    {
        sessionsDatabaseFile.delete();
        
        try (Storage sessionsStorage = new SqliteStorage("jdbc:sqlite:" + sessionsDatabaseFile))
        {
            sessionsStorage.connect();
            sessionsStorage.createUnit("sessions", new HashtableBuilder<String, Type>()
                .add("username", Type.TINYTEXT)
                .add("status", Type.INTEGER)
                .add("ip", Type.TINYTEXT)
                .build());
            sessionsStorage.setAutobatchEnabled(true);
            
            for (Player player : Bukkit.getOnlinePlayers())
            {
                String username = player.getName().toLowerCase();
                
                sessionsStorage.addEntry("sessions", new HashtableBuilder<String, String>()
                        .add("username", username)
                        .add("status", String.valueOf(getSession(username).getStatus()))
                        .add("ip", getPlayerIp(player))
                        .build());
            }
            
            sessionsStorage.executeBatch();
            sessionsStorage.clearBatch();
            sessionsStorage.setAutobatchEnabled(false);
        }
    }
    
    public void importSessions(File file) throws IOException
    {
        try (Storage sessionsStorage = new SqliteStorage("jdbc:sqlite:" + file))
        {
            sessionsStorage.connect();
            
            Player[] players = Bukkit.getOnlinePlayers();
            
            for (Player player : players)
            {
                String username = player.getName().toLowerCase();
                
                if (getSession(username) == null)
                {
                    createSession(username, "");
                }
                
                List<Hashtable<String, String>> rs = sessionsStorage.selectEntries("sessions",
                        Arrays.asList("username", "status", "ip"),
                        new SelectorCondition("username", Infix.EQUALS, username));
                
                if (!rs.isEmpty())
                {
                    Session session = getSession(username);
                    
                    session.setStatus(Integer.parseInt(rs.get(0).get("status")));
                    session.setIp(rs.get(0).get("ip"));
                }
            }
        }
    }
    
    public static final long TASK_PERIOD = 1;
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
}
