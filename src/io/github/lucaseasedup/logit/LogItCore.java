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
package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.listener.ServerEventListener;
import io.github.lucaseasedup.logit.listener.EntityEventListener;
import io.github.lucaseasedup.logit.listener.SessionEventListener;
import io.github.lucaseasedup.logit.listener.PlayerEventListener;
import io.github.lucaseasedup.logit.listener.TickEventListener;
import io.github.lucaseasedup.logit.listener.BlockEventListener;
import io.github.lucaseasedup.logit.listener.AccountEventListener;
import io.github.lucaseasedup.logit.listener.InventoryEventListener;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import io.github.lucaseasedup.logit.db.Pinger;
import io.github.lucaseasedup.logit.db.MySqlDatabase;
import io.github.lucaseasedup.logit.db.AbstractSqlDatabase;
import io.github.lucaseasedup.logit.command.LogItCommand;
import io.github.lucaseasedup.logit.command.LoginCommand;
import io.github.lucaseasedup.logit.command.UnregisterCommand;
import io.github.lucaseasedup.logit.command.ChangePassCommand;
import io.github.lucaseasedup.logit.command.LogoutCommand;
import io.github.lucaseasedup.logit.command.RegisterCommand;
import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.account.AccountWatcher;
import static io.github.lucaseasedup.logit.hash.HashGenerator.*;
import io.github.lucaseasedup.logit.session.SessionManager;
import com.google.common.collect.ImmutableList;
import io.github.lucaseasedup.logit.db.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.stripColor;
import org.bukkit.entity.Player;

/**
 * LogItCore is the central part of LogIt.
 * </p>
 * It's also the most important part of API.
 * 
 * @author LucasEasedUp
 */
public class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    public void start()
    {
        if (started)
            return;
        
        new File(plugin.getDataFolder(), "lib").mkdir();
        
        config = new LogItConfiguration(plugin);
        config.load();
        
        if (!loaded)
            load();
        
        if (getHashingAlgorithm().equals(HashingAlgorithm.UNKNOWN))
        {
            log(SEVERE, getMessage("UNKNOWN_HASHING_ALGORITHM").replace("%ha%", getHashingAlgorithm().name()));
            plugin.disable();
            
            return;
        }
        
        try
        {
            switch (getStorageAccountsDbType())
            {
                case SQLITE:
                {
                    database = new SqliteDatabase("jdbc:sqlite:" +
                        plugin.getDataFolder() + "/" + config.getString("storage.accounts.sqlite.filename"));
                    database.connect(null, null, null);
                    
                    break;
                }
                case MYSQL:
                {
                    database = new MySqlDatabase(config.getString("storage.accounts.mysql.host"));
                    database.connect(config.getString("storage.accounts.mysql.user"), config.getString("storage.accounts.mysql.password"),
                        config.getString("storage.accounts.mysql.database"));
                    
                    break;
                }
                case H2:
                {
                    database = new H2Database("jdbc:h2:" +
                        plugin.getDataFolder() + "/" + config.getString("storage.accounts.h2.filename"));
                    database.connect(null, null, null);
                    
                    break;
                }
                default:
                {
                    log(SEVERE, getMessage("UNKNOWN_STORAGE_TYPE").replace("%st%", getStorageAccountsDbType().name()));
                    plugin.disable();
                    
                    return;
                }
            }
        }
        catch (SQLException | ReflectiveOperationException ex)
        {
            Logger.getLogger(LogItCore.class.getName()).log(Level.SEVERE, null, ex);
            plugin.disable();
            
            return;
        }
        
        pinger = new Pinger(database);
        
        try
        {
            String[] storageColumnsArray = getStorageColumns();
            
            database.createTableIfNotExists(config.getString("storage.accounts.table"), storageColumnsArray);
            
            Set<String> existingColumns = database.getColumnNames(config.getString("storage.accounts.table"));
            
            database.setAutobatchEnabled(true);
            
            for (int i = 0; i < storageColumnsArray.length; i += 2)
            {
                if (!existingColumns.contains(storageColumnsArray[i]))
                {
                    database.addColumn(config.getString("storage.accounts.table"), storageColumnsArray[i], storageColumnsArray[i + 1]);
                }
            }
            
            database.executeBatch();
            database.setAutobatchEnabled(false);
        }
        catch (SQLException ex)
        {
            Logger.getLogger(LogItCore.class.getName()).log(Level.SEVERE, null, ex);
            plugin.disable();
            
            return;
        }
        
        accountManager = new AccountManager(this, database);
        
        try
        {
            accountManager.loadAccounts();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(LogItCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        accountWatcher = new AccountWatcher(this, accountManager);
        backupManager  = new BackupManager(this, database);
        sessionManager = new SessionManager(this, accountManager);
        
        SqliteDatabase inventoryDatabase = new SqliteDatabase("jdbc:sqlite:" +
            plugin.getDataFolder() + "/" + config.getString("storage.inventories.filename"));
        
        try
        {
            inventoryDatabase.connect();
            inventoryDatabase.createTableIfNotExists("inventories", new String[]{
                "username",     "VARCHAR(16)",
                "world",        "VARCHAR(512)",
                "inv-contents", "TEXT",
                "inv-armor",    "TEXT"
            });
            
            ResultSet rs = inventoryDatabase.select("inventories", new String[]{"username", "world", "inv-contents", "inv-armor"});
            
            while (rs.next())
            {
                try
                {
                    InventoryDepository.saveInventory(rs.getString("world"), rs.getString("username"),
                        InventoryDepository.unserialize(rs.getString("inv-contents")),
                        InventoryDepository.unserialize(rs.getString("inv-armor")));
                }
                catch (FileNotFoundException ex)
                {
                }
            }
            
            inventoryDatabase.truncateTable("inventories");
        }
        catch (IOException | SQLException ex)
        {
            Logger.getLogger(LogItCore.class.getName()).log(Level.SEVERE, null, ex);
            plugin.disable();
            
            return;
        }
        
        inventoryDepository = new InventoryDepository(inventoryDatabase);
        
        pingerTaskId          = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, pinger, 0L, 2400L);
        sessionManagerTaskId  = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, sessionManager, 0L, 20L);
        tickEventCallerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, tickEventCaller, 0L, 1L);
        accountWatcherTaskId  = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, accountWatcher, 0L, 12000L);
        backupManagerTaskId   = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, backupManager, 0L, 40L);
        
        log(FINE, getMessage("PLUGIN_START_SUCCESS")
                .replace("%st%", getStorageAccountsDbType().name())
                .replace("%ha%", getHashingAlgorithm().name()));
        
        // Set started to true to prevent starting multiple times.
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
            Logger.getLogger(LogItCore.class.getName()).log(Level.WARNING, null, ex);
        }
        
        Bukkit.getScheduler().cancelTask(pingerTaskId);
        Bukkit.getScheduler().cancelTask(sessionManagerTaskId);
        Bukkit.getScheduler().cancelTask(tickEventCallerTaskId);
        Bukkit.getScheduler().cancelTask(accountWatcherTaskId);
        Bukkit.getScheduler().cancelTask(backupManagerTaskId);
        
        log(FINE, getMessage("PLUGIN_STOP_SUCCESS"));
        
        // Set started to false to allow starting again.
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
        config.set("password.global-password", hash(password));
        
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
        return config.getString("password.global-password").equals(hash(password));
    }
    
    public void removeGlobalPassword()
    {
        config.set("password.global-password", "");
        
        log(INFO, getMessage("GLOBALPASS_REMOVE_SUCCESS"));
    }
    
    /**
     * Checks if the player is forced to login (by either "force-login.global" set to true, or
     * the player being in a world with forced login).
     * <p/>
     * If the player has the "logit.force-login.exempt" permission, it returns false.
     * 
     * @param player Player.
     * @return True if the specified player is forced to log in.
     */
    public boolean isPlayerForcedToLogin(Player player)
    {
        return (config.getBoolean("force-login.global") || config.getStringList("force-login.in-worlds").contains(player.getWorld().getName()))
                && !player.hasPermission("logit.force-login.exempt");
    }
    
    /**
     * Updates player group depending on whether they're logged in or logged out.
     * 
     * @param player Player whose group is to be updated.
     */
    public void updatePlayerGroup(Player player)
    {
        if (!isLinkedToVault())
            return;
        
        if (sessionManager.isSessionAlive(player))
        {
            permissions.playerRemoveGroup(player, config.getString("groups.logged-out"));
            permissions.playerAddGroup(player, config.getString("groups.logged-in"));
        }
        else
        {
            permissions.playerRemoveGroup(player, config.getString("groups.logged-in"));
            permissions.playerAddGroup(player, config.getString("groups.logged-out"));
        }
    }
    
    /**
     * Checks if LogIt is linked to Vault (e.i.&nbsp;LogItCore has been loaded and Vault is enabled).
     * 
     * @return True if LogIt is linked to Vault.
     */
    public boolean isLinkedToVault()
    {
        return permissions != null;
    }
    
    /**
     * Hashes the given string through algorithm specified in the config.
     * 
     * @param string String to be hashed.
     * @return Resulting hash.
     */
    public String hash(String string)
    {
        switch (getHashingAlgorithm())
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
     * Hashes the given string and salt through algorithm specified in the config.
     * <p/>
     * If algorithm is PLAIN, salt won't be appended to the string.
     * 
     * @param string String to be hashed.
     * @param salt Salt.
     * @return Resulting hash.
     */
    public String hash(String string, String salt)
    {
        String hash;
        
        if (getHashingAlgorithm() != HashingAlgorithm.PLAIN)
            hash = hash(string + salt);
        else
            hash = hash(string);
        
        return hash;
    }
    
    public StorageType getStorageAccountsDbType()
    {
        String s = plugin.getConfig().getString("storage.accounts.db-type");
        
        if (s.equalsIgnoreCase("sqlite"))
        {
            return StorageType.SQLITE;
        }
        else if (s.equalsIgnoreCase("mysql"))
        {
            return StorageType.MYSQL;
        }
        else if (s.equalsIgnoreCase("h2"))
        {
            return StorageType.H2;
        }
        else
        {
            return StorageType.UNKNOWN;
        }
    }
    
    public HashingAlgorithm getHashingAlgorithm()
    {
        String s = plugin.getConfig().getString("hashing-algorithm");
        
        if (s.equalsIgnoreCase("plain"))
        {
            return HashingAlgorithm.PLAIN;
        }
        else if (s.equalsIgnoreCase("md2"))
        {
            return HashingAlgorithm.MD2;
        }
        else if (s.equalsIgnoreCase("md5"))
        {
            return HashingAlgorithm.MD5;
        }
        else if (s.equalsIgnoreCase("sha-1"))
        {
            return HashingAlgorithm.SHA1;
        }
        else if (s.equalsIgnoreCase("sha-256"))
        {
            return HashingAlgorithm.SHA256;
        }
        else if (s.equalsIgnoreCase("sha-384"))
        {
            return HashingAlgorithm.SHA384;
        }
        else if (s.equalsIgnoreCase("sha-512"))
        {
            return HashingAlgorithm.SHA512;
        }
        else if (s.equalsIgnoreCase("whirlpool"))
        {
            return HashingAlgorithm.WHIRLPOOL;
        }
        else
        {
            return HashingAlgorithm.UNKNOWN;
        }
    }
    
    public IntegrationType getIntegration()
    {
        String s = plugin.getConfig().getString("integration");
        
        if (s.equalsIgnoreCase("none"))
        {
            return IntegrationType.NONE;
        }
        else if (s.equalsIgnoreCase("phpbb"))
        {
            return IntegrationType.PHPBB;
        }
        else
        {
            return IntegrationType.UNKNOWN;
        }
    }
    
    /**
     * Logs a message in the name of LogIt.
     * 
     * @param level Message level.
     * @param message Message.
     */
    public void log(Level level, String message)
    {
        if (config.getBoolean("log-to-file.enabled"))
        {
            Date             date = new Date();
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            try (FileWriter fileWriter = new FileWriter(new File(plugin.getDataFolder(), config.getString("log-to-file.filename")), true))
            {
                fileWriter.write(sdf.format(date) + " [" + level.getName() + "] " + stripColor(message) + "\n");
            }
            catch (IOException ex)
            {
            }
        }
        
        plugin.getLogger().log(level, stripColor(message));
    }
    
    /**
     * Returns an array containing storage columns,
     * where getStorageColumns()[i] is the column name,
     * and getStorageColumns()[i + 1] is the column type.
     * 
     * @return Storage columns.
     */
    public String[] getStorageColumns()
    {
        return storageColumns.toArray(new String[storageColumns.size()]);
    }
    
    public Permission getPermissions()
    {
        return permissions;
    }
    
    public WaitingRoom getWaitingRoom()
    {
        return waitingRoom;
    }
    
    public AbstractSqlDatabase getDatabase()
    {
        return database;
    }
    
    public InventoryDepository getInventoryDepository()
    {
        return inventoryDepository;
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
        storageColumns = ImmutableList.of(
            config.getString("storage.accounts.columns.username"),    "VARCHAR(16)",
            config.getString("storage.accounts.columns.salt"),        "VARCHAR(20)",
            config.getString("storage.accounts.columns.password"),    "VARCHAR(256)",
            config.getString("storage.accounts.columns.ip"),          "VARCHAR(64)",
            config.getString("storage.accounts.columns.last_active"), "INTEGER"
        );
        
        waitingRoom     = new WaitingRoom(this);
        tickEventCaller = new TickEventCaller();
        
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
        
        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault"))
        {
            permissions = plugin.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        }
        
        loaded = true;
    }
    
    /**
     * The preferred way to obtain the instance of LogIt core.
     * 
     * @return Instance of LogIt core.
     */
    public static LogItCore getInstance()
    {
        return INSTANCE;
    }
    
    public static enum StorageType
    {
        UNKNOWN, SQLITE, MYSQL, H2
    }
    
    public static enum HashingAlgorithm
    {
        UNKNOWN, PLAIN, MD2, MD5, SHA1, SHA256, SHA384, SHA512, WHIRLPOOL
    }
    
    public static enum IntegrationType
    {
        UNKNOWN, NONE, PHPBB
    }
    
    private static final LogItCore INSTANCE = new LogItCore((LogItPlugin) Bukkit.getPluginManager().getPlugin("LogIt"));
    
    private final LogItPlugin plugin;
    
    private boolean loaded = false;
    private boolean started = false;
    
    private LogItConfiguration  config;
    private AbstractSqlDatabase database;
    private Pinger              pinger;
    private Permission          permissions;
    private SessionManager      sessionManager;
    private AccountManager      accountManager;
    private AccountWatcher      accountWatcher;
    private BackupManager       backupManager;
    private WaitingRoom         waitingRoom;
    private InventoryDepository inventoryDepository;
    private TickEventCaller     tickEventCaller;
    
    private int pingerTaskId;
    private int sessionManagerTaskId;
    private int tickEventCallerTaskId;
    private int accountWatcherTaskId;
    private int backupManagerTaskId;
    
    private List<String> storageColumns;
}
