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
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import com.gmail.lucaseasedup.logit.account.AccountManager;
import com.gmail.lucaseasedup.logit.command.*;
import com.gmail.lucaseasedup.logit.db.*;
import static com.gmail.lucaseasedup.logit.hash.HashGenerator.*;
import com.gmail.lucaseasedup.logit.listener.*;
import com.gmail.lucaseasedup.logit.session.SessionManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.stripColor;
import org.bukkit.entity.Player;

/**
 * The LogIt core.
 * 
 * @author LucasEasedUp
 */
public class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    /**
     * Starts the LogIt core.
     */
    public void start()
    {
        // If LogIt has already been started, return at this point.
        if (started)
            return;
        
        // If LogIt hasn't been loaded yet, do it now.
        if (!loaded)
            load();
        
        config.load();
        
        if (plugin.getServer().getOnlineMode() && config.isStopOnOnlineModeEnabled())
        {
            log(FINE, getMessage("ONLINEMODE_ENABLED"));
            plugin.disable();
            
            return;
        }
        
        // If LogIt does not know the hashing algorithm specified in the config, stop.
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
                    database.connect("jdbc:sqlite:" + plugin.getDataFolder() + "/" + config.getSqliteFilename(), null, null, null);
                    
                    break;
                }
                case MYSQL:
                {
                    database = new MySqlDatabase();
                    database.connect(config.getMysqlHost(), config.getMysqlUser(), config.getMysqlPassword(), config.getMysqlDatabase());
                    
                    break;
                }
                default:
                {
                    log(SEVERE, getMessage("UNKNOWN_STORAGE_TYPE"));
                    plugin.disable();
                    
                    return;
                }
            }
        }
        catch (SQLException ex)
        {
            log(SEVERE, getMessage("DB_CONNECT_FAIL"));
            plugin.disable();
            
            return;
        }
        
        pinger = new Pinger(database);
        
        try
        {
            // Create a table for LogIt if it does not exist.
            database.create(config.getStorageTable(), "username varchar(16) NOT NULL,"
                                                    + "salt varchar(20) NOT NULL,"
                                                    + "password varchar(256) NOT NULL,"
                                                    + "ip varchar(64)");
        }
        catch (SQLException ex)
        {
            log(SEVERE, getMessage("DB_CREATE_TABLE_FAIL"));
            plugin.disable();
            
            return;
        }
        
        // At this point, we can surely say that LogIt has successfully started.
        log(FINE, getMessage("PLUGIN_START_SUCCESS").replace("%st%", config.getStorageType().name())
                .replace("%ha%", config.getHashingAlgorithm().name()));
        
        accountManager = new AccountManager(this, database);
        accountManager.loadAccounts();
        
        backupManager = new BackupManager(this, database);
        
        pingerTaskId          = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, pinger, 0L, 2400L);
        sessionManagerTaskId  = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, sessionManager, 0L, 20L);
        tickEventCallerTaskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, tickEventCaller, 0L, 1L);
        backupManagerTaskId   = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, backupManager, 0L, 40L);
        
        // Set started to true to prevent starting multiple times.
        started = true;
    }
    
    /**
     * Stops the LogIt core.
     */
    public void stop()
    {
        // If LogIt is not started, return at this point.
        if (!started)
            return;
        
        try
        {
            database.close();
        }
        catch (SQLException ex)
        {
            log(WARNING, getMessage("DB_CLOSE_FAIL"));
        }
        
        Bukkit.getScheduler().cancelTask(pingerTaskId);
        Bukkit.getScheduler().cancelTask(sessionManagerTaskId);
        Bukkit.getScheduler().cancelTask(tickEventCallerTaskId);
        Bukkit.getScheduler().cancelTask(backupManagerTaskId);
        
        log(FINE, getMessage("PLUGIN_STOP_SUCCESS"));
        
        // Set started to false to allow starting again.
        started = false;
    }
    
    /**
     * Restarts the LogIt core.
     */
    public void restart()
    {
        // Stop and start the LogIt core.
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
    
    /**
     * Checks if the given password matches the global password.
     * 
     * @param password Password to check.
     * @return True if they match.
     */
    public boolean checkGlobalPassword(String password)
    {
        return config.getGlobalPassword().equals(hash(password));
    }
    
    public void removeGlobalPassword()
    {
        config.setGlobalPassword("");
        config.save();
        
        log(INFO, getMessage("GLOBALPASS_REMOVE_SUCCESS"));
    }
    
    /**
     * Checks if the player is forced to login (by either "force-login" being set to true, or
     * the player being in a world with forced login). If the player has the "logit.login.exempt"
     * permission, it always returns false.
     * 
     * @param player Player.
     * @return True if the specified player is forced to log in.
     */
    public boolean isPlayerForcedToLogin(Player player)
    {
        return (config.getForceLoginGlobal() || config.getForceLoginInWorld(player.getWorld())) && !player.hasPermission("logit.login.exempt");
    }
    
    public void updatePlayerGroup(Player player)
    {
        if (sessionManager.isSessionAlive(player))
        {
            permissions.playerRemoveGroup(player, config.getGroupsLoggedOut());
            permissions.playerAddGroup(player, config.getGroupsLoggedIn());
        }
        else
        {
            permissions.playerRemoveGroup(player, config.getGroupsLoggedIn());
            permissions.playerAddGroup(player, config.getGroupsLoggedOut());
        }
    }
    
    public boolean isLinkedToVault()
    {
        return permissions != null;
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
    
    public String hash(String string, String salt)
    {
        String hash;
        
        if (config.getHashingAlgorithm() != PLAIN)
            hash = hash(string + salt);
        else
            hash = hash(string);
        
        return hash;
    }
    
    public String generateSalt()
    {
        SecureRandom sr   = new SecureRandom();
        byte[]       salt = new byte[20];
        
        sr.nextBytes(salt);
        
        return new String(salt);
    }
    
    public void log(Level level, String message)
    {
        if (config.isLogToFileEnabled())
        {
            Date             date = new Date();
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            try (FileWriter fileWriter = new FileWriter(new File(plugin.getDataFolder(), config.getLogFilename()), true))
            {
                fileWriter.write(sdf.format(date) + " [" + level.getName() + "] " + stripColor(message) + "\n");
            }
            catch (IOException ex)
            {
            }
        }
        
        plugin.getLogger().log(level, stripColor(message));
    }
    
    public Permission getPermissions()
    {
        return permissions;
    }
    
    public WaitingRoom getWaitingRoom()
    {
        return waitingRoom;
    }
    
    public Database getDatabase()
    {
        return database;
    }

    public BackupManager getBackupManager()
    {
        return backupManager;
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
        
        plugin.getServer().getPluginManager().registerEvents(new TickEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ServerEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AccountEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SessionEventListener(this), plugin);
        
        plugin.getCommand("logit").setExecutor(new LogItCommand(this));
        plugin.getCommand("login").setExecutor(new LoginCommand(this));
        plugin.getCommand("logout").setExecutor(new LogoutCommand(this));
        plugin.getCommand("register").setExecutor(new RegisterCommand(this));
        plugin.getCommand("unregister").setExecutor(new UnregisterCommand(this));
        plugin.getCommand("changepass").setExecutor(new ChangePassCommand(this));
        
        sessionManager = new SessionManager(this);
        tickEventCaller = new TickEventCaller();
        waitingRoom = new WaitingRoom(this);
        
        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault"))
        {
            permissions = plugin.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        }
        
        loaded = true;
    }
    
    public static LogItCore getInstance()
    {
        return INSTANCE;
    }
    
    private static final LogItCore INSTANCE = new LogItCore((LogItPlugin) Bukkit.getPluginManager().getPlugin("LogIt"));
    
    private final LogItPlugin plugin;
    
    private boolean loaded = false;
    private boolean started = false;
    
    private LogItConfiguration config;
    private Database database;
    
    private Pinger pinger;
    private int pingerTaskId;
    
    private SessionManager sessionManager;
    private int sessionManagerTaskId;
    
    private TickEventCaller tickEventCaller;
    private int tickEventCallerTaskId;
    
    private BackupManager backupManager;
    private int backupManagerTaskId;
    
    private AccountManager accountManager;
    private WaitingRoom waitingRoom;
    
    private Permission permissions;
}
