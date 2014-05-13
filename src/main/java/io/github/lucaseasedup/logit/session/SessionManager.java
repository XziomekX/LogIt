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
import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SqliteStorage;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.storage.Storage.DataType;
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

public final class SessionManager extends LogItCoreObject implements Runnable, Disposable
{
    @Override
    public void dispose()
    {
        if (sessions != null)
        {
            sessions.clear();
            sessions = null;
        }
    }
    
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void run()
    {
        boolean timeoutEnabled = getConfig().getBoolean("force-login.timeout.enabled");
        long timeoutValue = getConfig().getTime("force-login.timeout.value", TimeUnit.TICKS);
        
        List<String> disableTimeoutForPlayers =
                getConfig().getStringList("force-login.timeout.disable-for-players");
        long inactivityTimeToLogOut =
                20L * getConfig().getTime("crowd-control.automatic-logout.inactivity-time", TimeUnit.SECONDS);
        
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            String  username = entry.getKey();
            Session session  = entry.getValue();
            Player  player   = getPlayer(username);
            
            // Player is logged in, either online or offline.
            if (session.getStatus() >= 0L)
            {
                if (isPlayerOnline(username))
                {
                    session.setStatus(0L);
                    
                    if (getConfig().getBoolean("crowd-control.automatic-logout.enabled"))
                    {
                        if (session.getInactivityTime() >= inactivityTimeToLogOut)
                        {
                            endSession(username);
                            
                            player.sendMessage(getMessage("END_SESSION_AUTOMATIC_SELF"));
                            
                            if (getCore().isPlayerForcedToLogIn(player))
                            {
                                getCore().getMessageDispatcher().sendForceLoginMessage(player);
                            }
                            
                            session.resetInactivityTime();
                        }
                        else
                        {
                            session.updateInactivityTime(TASK_PERIOD);
                        }
                    }
                }
                else
                {
                    destroySession(username);
                }
            }
            // Player is logged out but online.
            else if (isPlayerOnline(username))
            {
                if (!containsIgnoreCase(username, disableTimeoutForPlayers)
                        && getCore().isPlayerForcedToLogIn(player))
                {
                    if (timeoutEnabled && session.getStatus() <= -timeoutValue)
                    {
                        player.kickPlayer(getMessage("FORCE_LOGIN_TIMEOUT"));
                    }
                    else
                    {
                        session.updateStatus(-TASK_PERIOD);
                    }
                }
            }
            // Player is logged out and offline.
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
     * 
     * @return the session object.
     */
    public Session getSession(String username)
    {
        return sessions.get(username.toLowerCase());
    }
    
    /**
     * Returns a session attached to the given player.
     * 
     * @param player the player object.
     * 
     * @return the session object or {@code null} if no session
     *         has been created for this player.
     */
    public Session getSession(Player player)
    {
        return getSession(player.getName());
    }
    
    /**
     * Checks if a session is alive.
     * 
     * <p> Returns {@code true} if {@code player} is not {@code null},
     * the session exists, is alive and player IP matches session IP;
     * {@code false} otherwise.
     * 
     * @param player a player with the username representing a session.
     * 
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
     * <p> Returns {@code true} if {@code name} is not {@code null},
     * the session exists, is alive and, if the player is online,
     * player IP matches session IP; {@code false} otherwise.
     * 
     * @param name the player username.
     * 
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
     * Creates a session for a player.
     * 
     * <p> If a session for this player already exists,
     * no action will be taken.
     * 
     * @param player the player object.
     * 
     * @return a {@code CancellableState} indicating whether
     *         the operation was cancelled or not by a Bukkit event.
     */
    public CancelledState createSession(Player player)
    {
        return createSession(player.getName(), getPlayerIp(player));
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
        Session session = getSession(username);
        
        if (session == null)
            return CancelledState.NOT_CANCELLED;
        
        if (session.isAlive())
        {
            endSession(username);
        }
        
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
        Session session = getSession(username);
        
        if (session == null)
            throw new SessionNotFoundException();
        
        if (session.isAlive())
            return CancelledState.NOT_CANCELLED;
        
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
        Session session = getSession(username);
        
        if (session == null)
            throw new SessionNotFoundException();
        
        if (!session.isAlive())
            return CancelledState.NOT_CANCELLED;
        
        SessionEvent evt = new SessionEndEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        // End session.
        session.setStatus(-1L);
        
        log(Level.FINE, getMessage("END_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Exports all sessions in this session manager to a file.
     * 
     * @param file the file to which sessions will be exported.
     * 
     * @throws IOException if an I/O error occured.
     */
    public void exportSessions(File file) throws IOException
    {
        file.delete();
        
        try (Storage sessionsStorage = new SqliteStorage("jdbc:sqlite:" + file))
        {
            sessionsStorage.connect();
            sessionsStorage.createUnit("sessions", new Hashtable<String, DataType>()
                {{
                    put("username", DataType.TINYTEXT);
                    put("status", DataType.INTEGER);
                    put("ip", DataType.TINYTEXT);
                }});
            sessionsStorage.setAutobatchEnabled(true);
            
            for (Player player : Bukkit.getOnlinePlayers())
            {
                String username = player.getName().toLowerCase();
                
                sessionsStorage.addEntry("sessions", new Storage.Entry.Builder()
                        .put("username", username)
                        .put("status", String.valueOf(getSession(username).getStatus()))
                        .put("ip", getPlayerIp(player))
                        .build());
            }
            
            sessionsStorage.executeBatch();
            sessionsStorage.clearBatch();
            sessionsStorage.setAutobatchEnabled(false);
        }
    }
    
    /**
     * Imports all the sessions from a file to this session manager.
     * 
     * @param file the file from which sessions will be imported.
     * 
     * @throws IOException if an I/O error occured.
     */
    public void importSessions(File file) throws IOException
    {
        try (Storage sessionsStorage = new SqliteStorage("jdbc:sqlite:" + file))
        {
            sessionsStorage.connect();
            
            for (Player player : Bukkit.getOnlinePlayers())
            {
                String username = player.getName().toLowerCase();
                
                if (getSession(username) == null)
                {
                    createSession(username, "");
                }
                
                List<Storage.Entry> entries = sessionsStorage.selectEntries("sessions",
                        Arrays.asList("username", "status", "ip"),
                        new SelectorCondition("username", Infix.EQUALS, username));
                
                if (!entries.isEmpty())
                {
                    Session session = getSession(username);
                    
                    session.setStatus(Integer.parseInt(entries.get(0).get("status")));
                    session.setIp(entries.get(0).get("ip"));
                }
            }
        }
    }
    
    /**
     * Recommended task period of {@code SessionManager} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = 1;
    
    private Map<String, Session> sessions = new ConcurrentHashMap<>();
}
