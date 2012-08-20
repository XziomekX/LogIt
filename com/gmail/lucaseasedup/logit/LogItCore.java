/*
 * LogItCore.java
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

import static com.gmail.lucaseasedup.logit.LogItConfiguration.HashingAlgorithm.*;
import com.gmail.lucaseasedup.logit.LogItConfiguration.StorageType;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import com.gmail.lucaseasedup.logit.command.*;
import com.gmail.lucaseasedup.logit.db.Database;
import com.gmail.lucaseasedup.logit.db.MySqlDatabase;
import com.gmail.lucaseasedup.logit.db.SqliteDatabase;
import com.gmail.lucaseasedup.logit.event.*;
import static com.gmail.lucaseasedup.logit.hash.HashGenerator.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import static java.util.logging.Level.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public final class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    public void start()
    {
        if (started)
        {
            return;
        }
        else if (!loaded)
        {
            load();
        }
        
        config.load();
        
        if (config.getStopIfOnlineModeEnabled() && Bukkit.getServer().getOnlineMode())
        {
            if (config.getVerbose())
            {
                log(INFO, getMessage("ONLINEMODE_ENABLED"));
            }
            
            Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("LogIt"));
            
            return;
        }
        
        if (config.getStorageType().equals(StorageType.SQLITE))
        {
            database = new SqliteDatabase();
        }
        else if (config.getStorageType().equals(StorageType.MYSQL))
        {
            database = new MySqlDatabase();
        }
        else if (config.getStorageType().equals(StorageType.UNKNOWN))
        {
            log(SEVERE, getMessage("UNKNOWN_STORAGE_TYPE"));
            
            return;
        }
        
        if (config.getHashingAlgorithm().equals(UNKNOWN))
        {
            log(SEVERE, getMessage("UNKNOWN_HASHING_ALGORITHM"));
            
            return;
        }
        
        try
        {
            if (config.getStorageType().equals(StorageType.SQLITE))
            {
                database.connect(config.getStorageSqliteFilename(), null, null, null);
            }
            else if (config.getStorageType().equals(StorageType.MYSQL))
            {
                database.connect(config.getStorageMysqlHost(), config.getStorageMysqlUser(), config.getStorageMysqlPassword(), config.getStorageMysqlDatabase());
            }
        }
        catch (SQLException ex)
        {
            log(SEVERE, getMessage("FAILED_DB_CONNECT"));
            
            return;
        }
        
        try
        {
            database.create(config.getStorageTable(), config.getStorageColumnsUsername() + " varchar(16) NOT NULL, " + config.getStorageColumnsPassword() + " varchar(256) NOT NULL");
        }
        catch (SQLException ex)
        {
            log(SEVERE, getMessage("FAILED_DB_CREATE_TABLE"));
            
            return;
        }
        
        loadData();
        
        pinger = new Pinger(database);
        pingerTaskId = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, pinger, 0L, 2400L);
        
        sessionManager = new SessionManager(this);
        sessionManagerTaskId = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, sessionManager, 0L, 20L);
        
        tickEventCaller = new TickEventCaller();
        tickEventCallerTaskId = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, tickEventCaller, 0L, 1L);
        
        started = true;
    }
    
    public void stop()
    {
        if (!started)
            return;
        
        try
        {
            database.close();
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("FAILED_DB_DISCONNECT"));
        }
        
        Bukkit.getServer().getScheduler().cancelTask(pingerTaskId);
        Bukkit.getServer().getScheduler().cancelTask(sessionManagerTaskId);
        Bukkit.getServer().getScheduler().cancelTask(tickEventCallerTaskId);
        
        started = false;
    }
    
    public void restart()
    {
        stop();
        start();
    }
    
    public boolean isRegistered(String username)
    {
        return passwords.containsKey(username.toLowerCase());
    }
    
    public boolean isRegistered(Player player)
    {
        return isRegistered(player.getName());
    }
    
    public void register(String username, String password, boolean notify)
    {
        if (isRegistered(username))
            throw new RuntimeException("Player already registered.");
        
        String hash = hash(password);
        
        passwords.put(username.toLowerCase(), hash);
        
        try
        {
            database.insert(config.getStorageTable(), "\"" + username.toLowerCase() + "\", \"" + hash + "\"");
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("FAILED_SAVE_DATA"));
        }
        
        if (notify)
        {
            sendMessage(username, getMessage("REGISTERED_SELF"));
            
            if (config.getVerbose())
            {
                log(INFO, getMessage("REGISTERED_OTHERS").replace("%player%", username));
            }
        }
    }
    
    public void unregister(String username, boolean notify)
    {
        if (!isRegistered(username))
            throw new RuntimeException("Player not registered.");
        
        if (isPlayerOnline(username) && sessionManager.isSessionAlive(username) && config.getForceLogin())
        {
            putIntoWaitingRoom(getPlayer(username));
            sessionManager.endSession(getPlayer(username), true);
        }
        
        passwords.remove(username.toLowerCase());
        
        try
        {
            database.delete(config.getStorageTable(), config.getStorageColumnsUsername() + " = \"" + username.toLowerCase() + "\"");
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("FAILED_SAVE_DATA"));
        }
        
        if (notify)
        {
            sendMessage(username, getMessage("UNREGISTERED_SELF"));
            
            if (config.getVerbose())
            {
                log(INFO, getMessage("UNREGISTERED_OTHERS").replace("%player%", username));
            }
        }
    }
    
    public void changePassword(String username, String newPassword, boolean notify)
    {
        if (!isRegistered(username))
            throw new RuntimeException("Player not registered.");
        
        String hash = hash(newPassword);
        
        passwords.put(username.toLowerCase(), hash);
        
        try
        {
            database.update(config.getStorageTable(), config.getStorageColumnsPassword() + " = \"" + hash + "\"", config.getStorageColumnsUsername() + " = \"" + username.toLowerCase() + "\"");
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("FAILED_SAVE_DATA"));
        }
        
        if (notify)
        {
            sendMessage(username, getMessage("PASSWORD_CHANGED_SELF"));
            
            if (config.getVerbose())
            {
                log(INFO, getMessage("PASSWORD_CHANGED_OTHERS").replace("%player%", username));
            }
        }
    }
    
    public boolean checkPassword(String username, String password)
    {
        if (!isRegistered(username))
            throw new RuntimeException("Player not registered.");
        
        String currentHash = passwords.get(username.toLowerCase());
        String hashToBeChecked = hash(password);
        
        return (currentHash != null) && (hashToBeChecked != null) && currentHash.equals(hashToBeChecked);
    }
    
    public boolean checkGlobalPassword(String password)
    {
        return hash(password).equalsIgnoreCase(config.getGlobalPasswordHash());
    }
    
    public boolean isPlayerForcedToLogin(Player player)
    {
        return (config.getForceLogin() || config.getForceLoginInWorld(player.getWorld())) && !player.hasPermission("logit.login.exempt");
    }
    
    public boolean isPlayerForcedToLogin(String username)
    {
        if (!isPlayerOnline(username))
            throw new RuntimeException("Player not online.");
        
        return isPlayerForcedToLogin(getPlayer(username));
    }
    
    public void purge() throws SQLException
    {
        database.truncate(config.getStorageTable());
        passwords.clear();
    }
    
    public void sendEventPreventionMessage(Player player)
    {
        if (isRegistered(player))
        {
            player.sendMessage(getMessage("PLEASE_LOGIN"));
        }
        else
        {
            player.sendMessage(getMessage("PLEASE_REGISTER"));
        }
    }
    
    public boolean isInWaitingRoom(Player player)
    {
        return waitingRoom.containsKey(player.getName().toLowerCase());
    }
    
    public void putIntoWaitingRoom(Player player)
    {
        if (!config.getWaitingRoomEnabled() || !isPlayerForcedToLogin(player))
            return;
        
        waitingRoom.put(player.getName().toLowerCase(), player.getLocation().clone());
        player.teleport(config.getWaitingRoomLocation());
    }
    
    public void takeOutOfWaitingRoom(Player player)
    {
        if (!isInWaitingRoom(player))
            return;
        
        Location l = waitingRoom.remove(player.getName().toLowerCase());
        player.teleport(l);
    }
    
    /**
     * Creates a hash from the given string using the algorithm specified in the config file.
     * 
     * @param string String.
     * @return Hash.
     */
    public String hash(String string)
    {
        if (config.getHashingAlgorithm().equals(PLAIN))
        {
            return string;
        }
        if (config.getHashingAlgorithm().equals(MD2))
        {
            return getMd2(string);
        }
        else if (config.getHashingAlgorithm().equals(MD5))
        {
            return getMd5(string);
        }
        else if (config.getHashingAlgorithm().equals(SHA1))
        {
            return getSha1(string);
        }
        else if (config.getHashingAlgorithm().equals(SHA256))
        {
            return getSha256(string);
        }
        else if (config.getHashingAlgorithm().equals(SHA384))
        {
            return getSha384(string);
        }
        else if (config.getHashingAlgorithm().equals(SHA512))
        {
            return getSha512(string);
        }
        else if (config.getHashingAlgorithm().equals(WHIRLPOOL))
        {
            return getWhirlpool(string);
        }
        else
            return null;
    }
    
    public SessionManager getSessionManager()
    {
        return sessionManager;
    }
    
    public LogItPlugin getPlugin()
    {
        return plugin;
    }
    
    public LogItConfiguration getConfig()
    {
        return config;
    }
    
    private void loadData()
    {
        passwords.clear();
        
        try
        {
            try (ResultSet rs = database.select(config.getStorageTable(), "*"))
            {
                assert rs.getMetaData().getColumnCount() == 1;
                
                while (rs.next())
                {
                    passwords.put(rs.getString(config.getStorageColumnsUsername()), rs.getString(config.getStorageColumnsPassword()));
                }
            }
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("FAILED_LOAD_DATA"));
        }
    }
    
    private void load()
    {
        config = new LogItConfiguration(plugin);
        
        Bukkit.getServer().getPluginManager().registerEvents(new TickEventListener(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockEventListener(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityEventListener(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryEventListener(this), plugin);
        
        plugin.getCommand("logit").setExecutor(new LogItCommand(this));
        plugin.getCommand("login").setExecutor(new LoginCommand(this));
        plugin.getCommand("logout").setExecutor(new LogoutCommand(this));
        plugin.getCommand("register").setExecutor(new RegisterCommand(this));
        plugin.getCommand("unregister").setExecutor(new UnregisterCommand(this));
        plugin.getCommand("changepass").setExecutor(new ChangePassCommand(this));
        
        loaded = true;
    }
    
    public static LogItCore getInstance()
    {
        return LogItCore.LogItCoreHolder.INSTANCE;
    }
    
    private static class LogItCoreHolder
    {
        private static final LogItCore INSTANCE = new LogItCore((LogItPlugin) Bukkit.getPluginManager().getPlugin("LogIt"));
    }
    
    private LogItPlugin plugin;
    
    private boolean loaded = false;
    private boolean started = false;
    
    private LogItConfiguration config;
    private Database database;
    
    private HashMap<String, String> passwords = new HashMap<>();
    private HashMap<String, Location> waitingRoom = new HashMap<>();
    
    private Pinger pinger;
    private int pingerTaskId;
    
    private SessionManager sessionManager;
    private int sessionManagerTaskId;
    
    private TickEventCaller tickEventCaller;
    private int tickEventCallerTaskId;
}
