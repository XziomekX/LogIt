/*
 * LogItCore.java
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
package io.github.lucaseasedup.logit;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.LogItPlugin.parseMessage;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getBCrypt;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getMd2;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getMd5;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getSha1;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getSha256;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getSha384;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getSha512;
import static io.github.lucaseasedup.logit.hash.HashGenerator.getWhirlpool;
import static org.bukkit.ChatColor.stripColor;
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.account.AccountWatcher;
import io.github.lucaseasedup.logit.command.ChangeEmailCommand;
import io.github.lucaseasedup.logit.command.ChangePassCommand;
import io.github.lucaseasedup.logit.command.DisabledCommandExecutor;
import io.github.lucaseasedup.logit.command.LogItCommand;
import io.github.lucaseasedup.logit.command.LoginCommand;
import io.github.lucaseasedup.logit.command.LogoutCommand;
import io.github.lucaseasedup.logit.command.RecoverPassCommand;
import io.github.lucaseasedup.logit.command.RegisterCommand;
import io.github.lucaseasedup.logit.command.UnregisterCommand;
import io.github.lucaseasedup.logit.config.LogItConfiguration;
import io.github.lucaseasedup.logit.db.CsvDatabase;
import io.github.lucaseasedup.logit.db.Database;
import io.github.lucaseasedup.logit.db.H2Database;
import io.github.lucaseasedup.logit.db.MySqlDatabase;
import io.github.lucaseasedup.logit.db.Pinger;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import io.github.lucaseasedup.logit.db.Table;
import io.github.lucaseasedup.logit.hash.BCrypt;
import io.github.lucaseasedup.logit.hash.HashGenerator;
import io.github.lucaseasedup.logit.inventory.InventoryDepository;
import io.github.lucaseasedup.logit.listener.AccountEventListener;
import io.github.lucaseasedup.logit.listener.BlockEventListener;
import io.github.lucaseasedup.logit.listener.EntityEventListener;
import io.github.lucaseasedup.logit.listener.InventoryEventListener;
import io.github.lucaseasedup.logit.listener.PlayerEventListener;
import io.github.lucaseasedup.logit.listener.ServerEventListener;
import io.github.lucaseasedup.logit.listener.SessionEventListener;
import io.github.lucaseasedup.logit.listener.TickEventListener;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.session.SessionManager;
import io.github.lucaseasedup.logit.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * LogItCore is the central part of LogIt.
 * </p>
 * It's also the most important part of API.
 * 
 * @author LucasEasedUp
 */
public final class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        this.plugin = plugin;
        this.firstRun = !new File(plugin.getDataFolder(), "config.yml").exists();
    }
    
    public void start()
    {
        if (started)
            return;
        
        new File(plugin.getDataFolder(), "lib").mkdir();
        
        config = new LogItConfiguration(plugin);
        
        try
        {
            config.load();
        }
        catch (IOException ex)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not load the configuration file.", ex);
            plugin.disable();
            
            return;
        }
        
        if (firstRun)
        {
            new File(plugin.getDataFolder(), "backup").mkdir();
            new File(plugin.getDataFolder(), "mail").mkdir();
            new File(plugin.getDataFolder(), "lang").mkdir();
            
            File mailPasswordRecovery = new File(plugin.getDataFolder(), "mail/password-recovery.html");
            
            if (!mailPasswordRecovery.exists())
            {
                try
                {
                    FileUtils.extractResource("/password-recovery.html", mailPasswordRecovery);
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, "Could not copy resource password-recovery.html.", ex);
                }
            }
        }
        
        if (getDefaultHashingAlgorithm().equals(HashingAlgorithm.UNKNOWN))
        {
            log(Level.SEVERE, getMessage("UNKNOWN_HASHING_ALGORITHM")
                    .replace("%ha%", getDefaultHashingAlgorithm().name()));
            plugin.disable();
            
            return;
        }
        
        try
        {
            if (getStorageAccountsDbType().equals(StorageType.H2))
            {
                LogItPlugin.loadLibrary(LIB_H2);
            }
            
            if (config.getBoolean("password-recovery.enabled"))
            {
                LogItPlugin.loadLibrary(LIB_MAIL);
            }
        }
        catch (ReportedException ex)
        {
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
                    database.connect();
                    
                    break;
                }
                case MYSQL:
                {
                    database = new MySqlDatabase(config.getString("storage.accounts.mysql.host"));
                    ((MySqlDatabase) database).connect(
                        config.getString("storage.accounts.mysql.user"),
                        config.getString("storage.accounts.mysql.password"),
                        config.getString("storage.accounts.mysql.database")
                    );
                    
                    break;
                }
                case H2:
                {
                    database = new H2Database("jdbc:h2:" +
                        plugin.getDataFolder() + "/" + config.getString("storage.accounts.h2.filename"));
                    database.connect();
                    
                    break;
                }
                case CSV:
                {
                    database = new CsvDatabase(plugin.getDataFolder());
                    database.connect();
                    
                    break;
                }
                default:
                {
                    log(Level.SEVERE, getMessage("UNKNOWN_STORAGE_TYPE")
                            .replace("%st%", getStorageAccountsDbType().name()));
                    plugin.disable();
                    
                    return;
                }
            }
        }
        catch (SQLException ex)
        {
            log(Level.SEVERE, "Could not open database connection.", ex);
            plugin.disable();
            
            return;
        }
        
        pinger = new Pinger(database);
        accountTable = new Table(database, config.getString("storage.accounts.table"),
                config.getConfigurationSection("storage.accounts.columns"));
        
        try
        {
            accountTable.open();
        }
        catch (SQLException ex)
        {
            log(Level.SEVERE, "Could not open account table.", ex);
            plugin.disable();
            
            return;
        }
        
        accountManager = new AccountManager(this, accountTable);
        
        try
        {
            accountManager.loadAccounts();
        }
        catch (SQLException ex)
        {
            plugin.disable();
            
            return;
        }
        
        accountWatcher = new AccountWatcher(this, accountManager);
        backupManager  = new BackupManager(this, accountTable);
        sessionManager = new SessionManager(this, accountManager);
        
        if (!accountTable.isColumnDisabled("logit.accounts.email"))
        {
            if (config.getBoolean("password-recovery.enabled"))
            {
                mailSender = new MailSender();
                mailSender.configure(config.getString("mail.smtp-host"), config.getInt("mail.smtp-port"),
                    config.getString("mail.smtp-user"), config.getString("mail.smtp-password"));
            }
        }
        
        SqliteDatabase inventoryDatabase = new SqliteDatabase("jdbc:sqlite:" +
            plugin.getDataFolder() + "/" + config.getString("storage.inventories.filename"));
        
        try
        {
            inventoryDatabase.connect();
            inventoryDatabase.createTableIfNotExists("inventories", new String[]{
                "username",     "VARCHAR(16)",
                "world",        "VARCHAR(512)",
                "inv_contents", "TEXT",
                "inv_armor",    "TEXT"
            });
            
            List<Map<String, String>> rs = inventoryDatabase.select("inventories",
                    new String[]{"username", "world", "inv_contents", "inv_armor"});
            
            for (Map<String, String> row : rs)
            {
                try
                {
                    inventoryDepository.saveInventory(row.get("world"), row.get("username"),
                        inventoryDepository.unserialize(row.get("inv_contents")),
                        inventoryDepository.unserialize(row.get("inv_armor")));
                }
                catch (FileNotFoundException ex)
                {
                }
            }
            
            inventoryDatabase.truncateTable("inventories");
        }
        catch (IOException | SQLException | ReflectiveOperationException ex)
        {
            log(Level.SEVERE, "Inventories could not be restored.", ex);
            plugin.disable();
            
            return;
        }
        
        inventoryDepository = new InventoryDepository(this, inventoryDatabase);
        waitingRoom = new WaitingRoom(this, accountTable);
        tickEventCaller = new TickEventCaller();
        
        pingerTaskId          = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, pinger, 0L, 2400L);
        sessionManagerTaskId  = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, sessionManager, 0L, 20L);
        tickEventCallerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, tickEventCaller, 0L, 1L);
        accountWatcherTaskId  = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, accountWatcher, 0L, 12000L);
        backupManagerTaskId   = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, backupManager, 0L, 40L);
        
        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault"))
        {
            permissions = plugin.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        }
        
        registerEvents();
        setCommandExecutors();
        
        log(Level.FINE, getMessage("PLUGIN_START_SUCCESS")
                .replace("%st%", getStorageAccountsDbType().name())
                .replace("%ha%", getDefaultHashingAlgorithm().name()));
        
        if (firstRun)
        {
            log(Level.INFO, getMessage("PLUGIN_FIRST_RUN"));
        }
        
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
            log(Level.WARNING, "Could not close database connection.", ex);
        }
        
        Bukkit.getScheduler().cancelTask(pingerTaskId);
        Bukkit.getScheduler().cancelTask(sessionManagerTaskId);
        Bukkit.getScheduler().cancelTask(tickEventCallerTaskId);
        Bukkit.getScheduler().cancelTask(accountWatcherTaskId);
        Bukkit.getScheduler().cancelTask(backupManagerTaskId);
        
        log(Level.FINE, getMessage("PLUGIN_STOP_SUCCESS"));
        
        started = false;
    }
    
    public void restart()
    {
        File sessions = new File(plugin.getDataFolder() + "/" + config.getString("storage.sessions.filename"));
        
        try
        {
            sessionManager.exportSessions(sessions);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not export sessions.", ex);
        }
        
        stop();
        
        try
        {
            plugin.loadMessages();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not load messages.", ex);
        }
        
        start();
        
        try
        {
            sessionManager.importSessions(sessions);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not import sessions.", ex);
        }
        
        sessions.delete();
        
        log(Level.INFO, getMessage("RELOADED"));
    }
    
    /**
     * Checks if the given password matches its hashed equivalent.
     * 
     * @param password Plain-text password.
     * @param hashedPassword Hashed password.
     * @return True if passwords match.
     */
    public boolean checkPassword(String password, String hashedPassword, HashingAlgorithm ha)
    {
        if (ha == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.checkpw(password, hashedPassword);
        }
        else
        {
            return hashedPassword.equals(hash(password, ha));
        }
    }
    
    /**
     * Checks if the given password matches its hashed equivalent.
     * 
     * @param password Plain-text password.
     * @param hashedPassword Hashed password.
     * @param salt Salt.
     * @return True if passwords match.
     */
    public boolean checkPassword(String password, String hashedPassword, String salt, HashingAlgorithm ha)
    {
        if (ha == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.checkpw(password, hashedPassword);
        }
        else
        {
            return hashedPassword.equals(hash(password, salt, ha));
        }
    }
    
    /**
     * Checks if the given password matches the global password.
     * 
     * @param password Plain-text password.
     * @return True if passwords match.
     */
    public boolean checkGlobalPassword(String password)
    {
        return checkPassword(password, config.getString("password.global-password.hash"),
            config.getString("password.global-password.salt"), getDefaultHashingAlgorithm());
    }

    /**
     * Changes the global password.
     * 
     * @param password New global password;
     */
    public void changeGlobalPassword(String password)
    {
        String salt = HashGenerator.generateSalt(getDefaultHashingAlgorithm());
        
        config.set("password.global-password.salt", salt);
        config.set("password.global-password.hash", hash(password, salt, getDefaultHashingAlgorithm()));
        
        log(Level.INFO, getMessage("GLOBALPASS_SET_SUCCESS"));
    }
    
    public void removeGlobalPassword()
    {
        config.set("password.global-password.hash", "");
        config.set("password.global-password.salt", "");
        
        log(Level.INFO, getMessage("GLOBALPASS_REMOVE_SUCCESS"));
    }
    
    public void sendPasswordRecoveryMail(String username) throws IOException, SQLException
    {
        try
        {
            if (mailSender == null)
                throw new RuntimeException("MailSender not initialized.");
            
            String to = accountManager.getEmail(username);
            String from = config.getString("mail.email-address");
            String subject = parseMessage(config.getString("password-recovery.subject"), new String[]{
                "%player%", username,
            });
            
            String playerPassword = generatePassword(config.getInt("password-recovery.password-length"),
                config.getString("password-recovery.password-combination"));
            accountManager.changeAccountPassword(username, playerPassword);
            
            File bodyTemplateFile =
                    new File(plugin.getDataFolder(), config.getString("password-recovery.body-template"));
            StringBuilder bodyBuilder = new StringBuilder();
            
            try (FileReader fr = new FileReader(bodyTemplateFile))
            {
                int b;

                while ((b = fr.read()) != -1)
                {
                    bodyBuilder.append((char) b);
                }
            }
            
            String body = parseMessage(bodyBuilder.toString(), new String[]{
                "%player%", username,
                "%password%", playerPassword
            });
            
            mailSender.sendMail(new String[]{to}, from, subject, body,
                    config.getBoolean("password-recovery.html-enabled"));
            
            log(Level.FINE, getMessage("RECOVER_PASSWORD_SUCCESS_LOG", new String[]{
                "%player%", username,
                "%email%", to,
            }));
        }
        catch (IOException | SQLException ex)
        {
            log(Level.WARNING, getMessage("RECOVER_PASSWORD_FAIL_LOG", new String[]{
                "%player%", username,
            }), ex);
            
            throw ex;
        }
    }
    
    public String generatePassword(int length, String combination)
    {
        char[] charTable = combination.toCharArray();
        
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        
        for (int i = 0, n = charTable.length; i < length; i++)
        {
            sb.append(charTable[random.nextInt(n)]);
        }
        
        return sb.toString();
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
        return (config.getBoolean("force-login.global")
                || config.getStringList("force-login.in-worlds").contains(player.getWorld().getName()))
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
        
        if (accountManager.isRegistered(player.getName()))
        {
            permissions.playerRemoveGroup(player, config.getString("groups.unregistered"));
            permissions.playerAddGroup(player, config.getString("groups.registered"));
        }
        else
        {
            permissions.playerRemoveGroup(player, config.getString("groups.registered"));
            permissions.playerAddGroup(player, config.getString("groups.unregistered"));
        }
        
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
    public String hash(String string, HashingAlgorithm ha)
    {
        switch (ha)
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
            case BCRYPT:
            {
                return getBCrypt(string, "");
            }
            default:
            {
                return null;
            }
        }
    }
    
    /**
     * Hashes the given string and salt through algorithm specified in the config.
     * 
     * @param string String to be hashed.
     * @param salt Salt.
     * @return Resulting hash.
     */
    public String hash(String string, String salt, HashingAlgorithm ha)
    {
        String hash;
        
        if (ha == HashingAlgorithm.BCRYPT)
        {
            hash = getBCrypt(string, salt);
        }
        else if (ha == HashingAlgorithm.PLAIN)
        {
            hash = hash(string, ha);
        }
        else
        {
            hash = hash(string + salt, ha);
        }
        
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
        else if (s.equalsIgnoreCase("csv"))
        {
            return StorageType.CSV;
        }
        else
        {
            return StorageType.UNKNOWN;
        }
    }
    
    public HashingAlgorithm getDefaultHashingAlgorithm()
    {
        return HashingAlgorithm.decode(plugin.getConfig().getString("password.hashing-algorithm"));
    }
    
    public IntegrationType getIntegration()
    {
        String s = plugin.getConfig().getString("integration");
        
        if (s.equalsIgnoreCase("none"))
        {
            return IntegrationType.NONE;
        }
        else if (s.equalsIgnoreCase("phpbb2"))
        {
            return IntegrationType.PHPBB2;
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            File logFile = new File(plugin.getDataFolder(), config.getString("log-to-file.filename"));
            
            try (FileWriter fileWriter = new FileWriter(logFile, true))
            {
                fileWriter.write(sdf.format(new Date()));
                fileWriter.write(" [");
                fileWriter.write(level.getName());
                fileWriter.write("] ");
                fileWriter.write(stripColor(message));
                fileWriter.write("\n");
            }
            catch (IOException ex)
            {
                plugin.getLogger().log(Level.WARNING, "Could not log to a file.", ex);
            }
        }
        
        plugin.getLogger().log(level, stripColor(message));
    }
    
    public void log(Level level, String message, Throwable t)
    {
        String stackTrace = null;
        
        try (
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
        )
        {
            t.printStackTrace(pw);
            
            stackTrace = sw.toString();
        }
        catch (IOException ex)
        {
        }
        
        if (stackTrace != null)
        {
            log(level, message + " [Exception stack trace:\n" + stackTrace + "]");
        }
        else
        {
            log(level, message + " [Unknown exception stack trace]");
        }
    }
    
    public Table getAccountTable()
    {
        return accountTable;
    }
    
    public Permission getPermissions()
    {
        return permissions;
    }
    
    public WaitingRoom getWaitingRoom()
    {
        return waitingRoom;
    }
    
    public InventoryDepository getInventoryDepository()
    {
        return inventoryDepository;
    }
    
    public MailSender getMailSender()
    {
        return mailSender;
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
    
    public boolean isFirstRun()
    {
        return firstRun;
    }
    
    public boolean isStarted()
    {
        return started;
    }
    
    private void setCommandExecutors()
    {
        plugin.getCommand("logit").setExecutor(new LogItCommand(this));
        plugin.getCommand("login").setExecutor(new LoginCommand(this));
        plugin.getCommand("logout").setExecutor(new LogoutCommand(this));
        plugin.getCommand("register").setExecutor(new RegisterCommand(this));
        plugin.getCommand("unregister").setExecutor(new UnregisterCommand(this));
        
        if (!accountTable.isColumnDisabled("logit.accounts.password"))
        {
            plugin.getCommand("changepass").setExecutor(new ChangePassCommand(this));
        }
        else
        {
            plugin.getCommand("changepass").setExecutor(new DisabledCommandExecutor(this));
        }
        
        if (!accountTable.isColumnDisabled("logit.accounts.email"))
        {
            plugin.getCommand("changeemail").setExecutor(new ChangeEmailCommand(this));
        }
        else
        {
            plugin.getCommand("changeemail").setExecutor(new DisabledCommandExecutor(this));
        }
        
        if (!accountTable.isColumnDisabled("logit.accounts.email")
                && config.getBoolean("password-recovery.enabled"))
        {
            plugin.getCommand("recoverpass").setExecutor(new RecoverPassCommand(this));
        }
        else
        {
            plugin.getCommand("recoverpass").setExecutor(new DisabledCommandExecutor(this));
        }
    }
    
    private void registerEvents()
    {
        plugin.getServer().getPluginManager().registerEvents(new TickEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ServerEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AccountEventListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SessionEventListener(this), plugin);
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
        UNKNOWN, SQLITE, MYSQL, H2, CSV
    }
    
    public static enum HashingAlgorithm
    {
        UNKNOWN, PLAIN, MD2, MD5, SHA1, SHA256, SHA384, SHA512, WHIRLPOOL, BCRYPT;
        
        public static HashingAlgorithm decode(String s)
        {
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
            else if (s.equalsIgnoreCase("bcrypt"))
            {
                return HashingAlgorithm.BCRYPT;
            }
            else
            {
                return HashingAlgorithm.UNKNOWN;
            }
        }
        
        public String encode()
        {
            switch (this)
            {
            case PLAIN:
                return "plain";
            case MD2:
                return "md2";
            case MD5:
                return "md5";
            case SHA1:
                return "sha-1";
            case SHA256:
                return "sha-256";
            case SHA384:
                return "sha-384";
            case SHA512:
                return "sha-512";
            case WHIRLPOOL:
                return "whirlpool";
            case BCRYPT:
                return "bcrypt";
            case UNKNOWN:
            default:
                return null;
            }
        }
    }
    
    public static enum IntegrationType
    {
        UNKNOWN, NONE, PHPBB2
    }
    
    public static final String LIB_H2 = "h2small-1.3.171.jar";
    public static final String LIB_MAIL = "mail-1.4.5.jar";
    
    private static final LogItCore INSTANCE = new LogItCore(LogItPlugin.getInstance());
    
    private final LogItPlugin plugin;
    
    private final boolean firstRun;
    private boolean started = false;
    
    private LogItConfiguration  config;
    private Database            database;
    private Table               accountTable;
    private Pinger              pinger;
    private Permission          permissions;
    private SessionManager      sessionManager;
    private AccountManager      accountManager;
    private AccountWatcher      accountWatcher;
    private BackupManager       backupManager;
    private WaitingRoom         waitingRoom;
    private InventoryDepository inventoryDepository;
    private MailSender          mailSender;
    private TickEventCaller     tickEventCaller;
    
    private int pingerTaskId;
    private int sessionManagerTaskId;
    private int tickEventCallerTaskId;
    private int accountWatcherTaskId;
    private int backupManagerTaskId;
}
