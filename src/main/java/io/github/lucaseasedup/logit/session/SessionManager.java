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
import io.github.lucaseasedup.logit.storage.SqliteStorage;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.storage.Storage.DataType;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SessionManager extends LogItCoreObject
        implements Iterable<Entry<String, Session>>, Runnable, Disposable
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
                getConfig().getTime("crowd-control.automatic-logout.inactivity-time",
                        TimeUnit.TICKS);
        
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
     * Returns a session associated with the given username.
     * 
     * @param username the username.
     * 
     * @return the {@code Session} object, or {@code null} if no session
     *         has been associated with this username.
     */
    public Session getSession(String username)
    {
        return sessions.get(username.toLowerCase());
    }
    
    /**
     * Returns a session associated with a player's name.
     * 
     * @param player the player.
     * 
     * @return the {@code Session} object, or {@code null} if no session
     *         has been associated with this player's name.
     */
    public Session getSession(Player player)
    {
        return getSession(player.getName());
    }
    
    /**
     * Checks whether a session associated with the given username is alive.
     * 
     * <p> Returns {@code true} if such session exists, is alive and,
     * if a player with this username is online, player IP matches session IP;
     * {@code false} otherwise.
     * 
     * @param username the username.
     * 
     * @return {@code true} if the session is alive; {@code false} otherwise.
     * 
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public boolean isSessionAlive(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        Session session = getSession(username);
        
        if (session == null)
            return false;
        
        if (PlayerUtils.isPlayerOnline(username))
        {
            Player player = getPlayer(username);
            String ip     = getPlayerIp(player);
            
            return session.isAlive() && ip.equals(session.getIp());
        }
        else
        {
            return session.isAlive();
        }
    }
    
    /**
     * Checks if a session associated with a player's name is alive.
     * 
     * <p> Returns {@code true} if such session exists, is alive
     * and player IP matches session IP; {@code false} otherwise.
     * 
     * @param player the player.
     * 
     * @return {@code true} if the session is alive; {@code false} otherwise.
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public boolean isSessionAlive(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        Session session = getSession(player.getName());
        
        if (session == null)
            return false;
        
        String ip = getPlayerIp(player);
        
        return session.isAlive() && ip.equals(session.getIp());
    }
    
    /**
     * Creates a new session and associates it with the given username.
     * 
     * <p> If a session with this username already exists,
     * no action will be taken.
     * 
     * <p> This method emits the {@code SessionCreateEvent} event.
     * 
     * @param username the username.
     * @param ip       the player IP address.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionCreateEvent} handlers.
     * 
     * @throws IllegalArgumentException if {@code username} or {@code ip} is {@code null}.
     */
    public CancelledState createSession(String username, String ip)
    {
        if (username == null || ip == null)
            throw new IllegalArgumentException();
        
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
     * Creates a new session and associates it with a player's name.
     * 
     * <p> If a session with a username equal to the player's name
     * already exists, no action will be taken.
     * 
     * <p> This method emits the {@code SessionCreateEvent} event.
     * 
     * @param player the player.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionCreateEvent} handlers.
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public CancelledState createSession(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        return createSession(player.getName(), getPlayerIp(player));
    }
    
    /**
     * Destroys a session associated with the given username.
     * 
     * <p> If a session with this username does not exist, no action will be taken.
     * If the session exists and is alive, this method will try to end it before proceeding.
     * 
     * <p> This method emits the {@code SessionDestroyEvent} event.
     * 
     * @param username the username.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionDestroyEvent} handlers.
     * 
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public CancelledState destroySession(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
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
     * Destroys a session associated with a player's name,
     * 
     * <p> If a session with this player's name does not exist, no action will be taken.
     * If the session exists and is alive, this method will try to end it before proceeding.
     * 
     * <p> This method emits the {@code SessionDestroyEvent} event.
     * 
     * @param player the player.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionDestroyEvent} handlers.
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public CancelledState destroySession(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        return destroySession(player.getName());
    }
    
    /**
     * Starts a session associated with the given username.
     * 
     * <p> If the session has already been started (e.i. is alive), no action will be taken.
     * 
     * <p> This method emits the {@code SessionStartEvent} event.
     * 
     * @param username the username.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionStartEvent} handlers.
     * 
     * @throws SessionNotFoundException if no such session exists.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public CancelledState startSession(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        Session session = getSession(username);
        
        if (session == null)
            throw new SessionNotFoundException(username);
        
        if (session.isAlive())
            return CancelledState.NOT_CANCELLED;
        
        SessionEvent evt = new SessionStartEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        // Start the session.
        session.setStatus(0L);
        
        log(Level.FINE, getMessage("START_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Starts a session associated with a player's name.
     * 
     * <p> If the session has already been started (e.i. is alive), no action will be taken.
     * 
     * <p> This method emits the {@code SessionStartEvent} event.
     * 
     * @param player the player.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionStartEvent} handlers.
     * 
     * @throws SessionNotFoundException if no such session exists.
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public CancelledState startSession(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        return startSession(player.getName());
    }
    
    /**
     * Ends a session associated with the given username.
     * 
     * <p> If the session is not alive, no action will be taken.
     * 
     * <p> This method emits the {@code SessionEndEvent} event.
     * 
     * @param username the username.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionEndEvent} handlers.
     * 
     * @throws SessionNotFoundException if no such session exists.
     * @throws IllegalArgumentException if {@code username} is {@code null}.
     */
    public CancelledState endSession(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        Session session = getSession(username);
        
        if (session == null)
            throw new SessionNotFoundException(username);
        
        if (!session.isAlive())
            return CancelledState.NOT_CANCELLED;
        
        SessionEvent evt = new SessionEndEvent(username, session);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return CancelledState.CANCELLED;
        
        // End the session.
        session.setStatus(-1L);
        
        log(Level.FINE, getMessage("END_SESSION_SUCCESS_LOG").replace("%player%", username));
        
        return CancelledState.NOT_CANCELLED;
    }
    
    /**
     * Ends a session associated with a player's name.
     * 
     * <p> If the session is not alive, no action will be taken.
     * 
     * <p> This method emits the {@code SessionEndEvent} event.
     * 
     * @param player the player.
     * 
     * @return a {@code CancellableState} indicating whether this operation
     *         has been cancelled by one of the {@code SessionEndEvent} handlers.
     * 
     * @throws SessionNotFoundException if no such session exists.
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public CancelledState endSession(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        return endSession(player.getName());
    }
    
    /**
     * Creates an iterator over the sessions in this {@code SessionManager}
     * with element removing disabled.
     * 
     * @return the session iterator.
     */
    @Override
    public Iterator<Entry<String, Session>> iterator()
    {
        return newSessionIterator();
    }
    
    private Iterator<Entry<String, Session>> newSessionIterator()
    {
        return new Iterator<Entry<String, Session>>()
        {
            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public Entry<String, Session> next()
            {
                return it.next();
            }
            
            @Override
            public boolean hasNext()
            {
                return it.hasNext();
            }
            
            private final Iterator<Entry<String, Session>> it = sessions.entrySet().iterator();
        };
    }
    
    /**
     * Exports all sessions from this {@code SessionManager} to a file.
     * 
     * <p> The file will be deleted before exporting.
     * 
     * @param file the file to which the sessions will be exported.
     * 
     * @throws IOException if an I/O error occured.
     * @throws IllegalArgumentException if {@code file} is {@code null}.
     */
    public void exportSessions(File file) throws IOException
    {
        if (file == null)
            throw new IllegalArgumentException();
        
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
            
            for (Entry<String, Session> e : sessions.entrySet())
            {
                sessionsStorage.addEntry("sessions", new Storage.Entry.Builder()
                        .put("username", e.getKey())
                        .put("status", String.valueOf(e.getValue().getStatus()))
                        .put("ip", e.getValue().getIp())
                        .build());
            }
            
            sessionsStorage.executeBatch();
            sessionsStorage.clearBatch();
            sessionsStorage.setAutobatchEnabled(false);
        }
    }
    
    /**
     * Imports all sessions from a file to this {@code SessionManager}.
     * 
     * <p> Only the sessions that don't exist in this {@code SessionManager} will be imported.
     * 
     * @param file the file from which the sessions will be imported.
     * 
     * @throws FileNotFoundException if no such file exists.
     * @throws IOException if an I/O error occured.
     * @throws IllegalArgumentException if {@code file} is {@code null}.
     */
    public void importSessions(File file) throws FileNotFoundException, IOException
    {
        if (file == null)
            throw new IllegalArgumentException();
        
        if (!file.exists())
            throw new FileNotFoundException();
        
        try (Storage sessionsStorage = new SqliteStorage("jdbc:sqlite:" + file))
        {
            sessionsStorage.connect();
            
            List<Storage.Entry> entries = sessionsStorage.selectEntries("sessions",
                    Arrays.asList("username", "status", "ip"));
            
            for (Storage.Entry entry : entries)
            {
                String username = entry.get("username");
                
                if (getSession(username) == null)
                {
                    String ip = entry.get("ip");
                    long status = Long.parseLong(entry.get("status"));
                    
                    createSession(username, ip);
                    
                    getSession(username).setStatus(status);
                }
            }
        }
    }
    
    /**
     * Recommended task period of {@code SessionManager} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = TimeUnit.TICKS.convert(1, TimeUnit.TICKS);
    
    private Map<String, Session> sessions = new ConcurrentHashMap<>();
}
