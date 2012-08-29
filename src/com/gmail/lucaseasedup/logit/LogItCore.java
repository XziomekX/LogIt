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
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import com.gmail.lucaseasedup.logit.command.*;
import com.gmail.lucaseasedup.logit.db.Database;
import com.gmail.lucaseasedup.logit.db.MySqlDatabase;
import com.gmail.lucaseasedup.logit.db.SqliteDatabase;
import com.gmail.lucaseasedup.logit.event.listener.*;
import static com.gmail.lucaseasedup.logit.hash.HashGenerator.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.stripColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

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
        
        if (plugin.getServer().getOnlineMode() && config.getStopIfOnlineModeEnabled())
        {
            log(FINE, getMessage("ONLINEMODE_ENABLED"));
            plugin.disable();
            
            return;
        }
        
        if (config.getHashingAlgorithm().equals(UNKNOWN))
        {
            log(SEVERE, getMessage("UNKNOWN_HASHING_ALGORITHM"));
            plugin.disable();
            
            return;
        }
        
        try
        {
            switch (config.getStorageType())
            {
                case SQLITE:
                {
                    database = new SqliteDatabase();
                    database.connect("jdbc:sqlite:" + plugin.getDataFolder() + "/" + config.getStorageSqliteFilename(), null, null, null);
                    
                    break;
                }
                case MYSQL:
                {
                    database = new MySqlDatabase();
                    database.connect(config.getStorageMysqlHost(), config.getStorageMysqlUser(), config.getStorageMysqlPassword(),
                        config.getStorageMysqlDatabase());
                    
                    break;
                }
                default:
                {
                    log(SEVERE, getMessage("UNKNOWN_STORAGE_TYPE"));
                    plugin.disable();
                    
                    return;
                }
            }
            
            pinger = new Pinger(database);
        }
        catch (SQLException ex)
        {
            log(SEVERE, getMessage("DB_CONNECT_FAIL"));
            plugin.disable();
            
            return;
        }
        
        try
        {
            database.create(config.getStorageTable(), config.getStorageColumnsUsername() + " varchar(16) NOT NULL, "
                    + config.getStorageColumnsPassword() + " varchar(256) NOT NULL, "
                    + config.getStorageColumnsIp() + " varchar(64) NOT NULL");
        }
        catch (SQLException ex)
        {
            log(SEVERE, getMessage("DB_CREATE_TABLE_FAIL"));
            plugin.disable();
            
            return;
        }
        
        // Load accounts.
        accountManager = new AccountManager(this);
        accountManager.loadAccounts();
        
        // Schedule tasks.
        pingerTaskId          = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, pinger, 0L, 2400L);
        sessionManagerTaskId  = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, sessionManager, 0L, 20L);
        tickEventCallerTaskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, tickEventCaller, 0L, 1L);
        
        started = true;
    }
    
    public void stop()
    {
        if (!started)
            return;
        
        try
        {
            // Disconnect from the database.
            database.close();
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("DB_CLOSE_FAIL"));
        }
        
        // Cancel tasks.
        plugin.getServer().getScheduler().cancelTask(pingerTaskId);
        plugin.getServer().getScheduler().cancelTask(sessionManagerTaskId);
        plugin.getServer().getScheduler().cancelTask(tickEventCallerTaskId);
        
        started = false;
    }
    
    public void restart()
    {
        stop();
        start();
        
        log(INFO, getMessage("RELOADED"));
    }
    
    /**
     * Changes the global password.
     * 
     * @param password New global password;
     */
    public void changeGlobalPassword(String password)
    {
        config.setGlobalPassword(hash(password));
        config.save();
        
        log(INFO, getMessage("GLOBALPASS_SET_SUCCESS"));
    }
    
    public boolean checkGlobalPassword(String password)
    {
        return config.getGlobalPassword().equals(hash(password));
    }
    
    /**
     * Removes the global password.
     */
    public void removeGlobalPassword()
    {
        config.setGlobalPassword("");
        config.save();
        
        log(INFO, getMessage("GLOBALPASS_REMOVE_SUCCESS"));
    }
    
    public boolean isPlayerForcedToLogin(Player player)
    {
        return (config.getForceLoginGlobal() || config.getForceLoginInWorld(player.getWorld()))
                && !player.hasPermission("logit.login.exempt");
    }
    
    public boolean isPlayerForcedToLogin(String username)
    {
        if (!isPlayerOnline(username))
        {
            throw new RuntimeException("Player not online.");
        }
        
        return isPlayerForcedToLogin(getPlayer(username));
    }
    
    public void sendForceLoginMessage(Player player)
    {
        if (accountManager.isAccountCreated(player.getName()))
        {
            player.sendMessage(getMessage("PLEASE_LOGIN"));
        }
        else
        {
            player.sendMessage(getMessage("PLEASE_REGISTER"));
        }
    }
    
    /**
     * Creates a hash from the given string using algorithm specified in the config file.
     * 
     * @param string String.
     * @return Hash.
     */
    public String hash(String string)
    {
        switch (config.getHashingAlgorithm())
        {
            case PLAIN:
            {
                return string;
            }
            case MD2:
            {
                return getMd2(string);
            }
            case MD5:
            {
                return getMd5(string);
            }
            case SHA1:
            {
                return getSha1(string);
            }
            case SHA256:
            {
                return getSha256(string);
            }
            case SHA384:
            {
                return getSha384(string);
            }
            case SHA512:
            {
                return getSha512(string);
            }
            case WHIRLPOOL:
            {
                return getWhirlpool(string);
            }
            default:
            {
                return null;
            }
        }
    }
    
    /**
     * Logs a message.
     * 
     * @param level Message level.
     * @param message Message to be logged.
     */
    public void log(Level level, String message)
    {
        if (config.getLogToFileEnabled())
        {
            Date             date = new Date();
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            try (FileWriter fileWriter = new FileWriter(plugin.getDataFolder() + "/" + config.getLogToFileFilename(), true))
            {
                fileWriter.write(sdf.format(date) + " [" + level.getName() + "] " + stripColor(message) + "\n");
            }
            catch (IOException ex)
            {
            }
        }
        
        if (config.getVerbose() || level.intValue() > FINE.intValue())
        {
            plugin.getLogger().log(level, stripColor(message));
        }
    }
    
    /**
     * Provides shortcut to the Bukkit.getServer().getPluginManager().callEvent() method.
     * 
     * @param event Event to be called.
     */
    public void callEvent(Event event)
    {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public WaitingRoom getWaitingRoom()
    {
        return waitingRoom;
    }
    
    public Database getDatabase()
    {
        return database;
    }

    public AccountManager getAccountManager()
    {
        return accountManager;
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
    
    private void load()
    {
        config = new LogItConfiguration(plugin);
        
        // Register event listeners.
        plugin.getServer().getPluginManager().registerEvents(new TickEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ServerEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AccountEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SessionEventListener(this), plugin);
        
        // Set command executors.
        plugin.getCommand("logit").setExecutor(new LogItCommand(this));
        plugin.getCommand("login").setExecutor(new LoginCommand(this));
        plugin.getCommand("logout").setExecutor(new LogoutCommand(this));
        plugin.getCommand("register").setExecutor(new RegisterCommand(this));
        plugin.getCommand("unregister").setExecutor(new UnregisterCommand(this));
        plugin.getCommand("changepass").setExecutor(new ChangePassCommand(this));
        
        sessionManager = new SessionManager(this);
        waitingRoom = new WaitingRoom();
        
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
    
    private final LogItPlugin plugin;
    
    private boolean loaded = false;
    private boolean started = false;
    
    private LogItConfiguration config;
    private Database database;
    
    private Pinger pinger;
    private int pingerTaskId;
    
    private SessionManager sessionManager;
    private int sessionManagerTaskId;
    
    private TickEventCaller tickEventCaller = new TickEventCaller();
    private int tickEventCallerTaskId;
    
    private AccountManager accountManager;
    private WaitingRoom waitingRoom;
}
