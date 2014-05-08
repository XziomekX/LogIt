/*
 * LogItCore.java
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
package io.github.lucaseasedup.logit;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.CollectionUtils.containsIgnoreCase;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.account.AccountWatcher;
import io.github.lucaseasedup.logit.backup.BackupManager;
import io.github.lucaseasedup.logit.command.AcclockCommand;
import io.github.lucaseasedup.logit.command.AccunlockCommand;
import io.github.lucaseasedup.logit.command.ChangeEmailCommand;
import io.github.lucaseasedup.logit.command.ChangePassCommand;
import io.github.lucaseasedup.logit.command.DisabledCommandExecutor;
import io.github.lucaseasedup.logit.command.LoginCommand;
import io.github.lucaseasedup.logit.command.LogoutCommand;
import io.github.lucaseasedup.logit.command.NopCommandExecutor;
import io.github.lucaseasedup.logit.command.ProfileCommand;
import io.github.lucaseasedup.logit.command.RecoverPassCommand;
import io.github.lucaseasedup.logit.command.RegisterCommand;
import io.github.lucaseasedup.logit.command.RememberCommand;
import io.github.lucaseasedup.logit.command.UnregisterCommand;
import io.github.lucaseasedup.logit.config.InvalidPropertyValueException;
import io.github.lucaseasedup.logit.config.LogItConfiguration;
import io.github.lucaseasedup.logit.listener.BlockEventListener;
import io.github.lucaseasedup.logit.listener.EntityEventListener;
import io.github.lucaseasedup.logit.listener.InventoryEventListener;
import io.github.lucaseasedup.logit.listener.PlayerEventListener;
import io.github.lucaseasedup.logit.listener.ServerEventListener;
import io.github.lucaseasedup.logit.listener.SessionEventListener;
import io.github.lucaseasedup.logit.locale.EnglishLocale;
import io.github.lucaseasedup.logit.locale.GermanLocale;
import io.github.lucaseasedup.logit.locale.LocaleManager;
import io.github.lucaseasedup.logit.locale.PolishLocale;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.persistence.AirBarSerializer;
import io.github.lucaseasedup.logit.persistence.ExperienceSerializer;
import io.github.lucaseasedup.logit.persistence.HealthBarSerializer;
import io.github.lucaseasedup.logit.persistence.HungerBarSerializer;
import io.github.lucaseasedup.logit.persistence.LocationSerializer;
import io.github.lucaseasedup.logit.persistence.PersistenceManager;
import io.github.lucaseasedup.logit.persistence.PersistenceSerializer;
import io.github.lucaseasedup.logit.profile.ProfileManager;
import io.github.lucaseasedup.logit.security.BCrypt;
import io.github.lucaseasedup.logit.security.HashingAlgorithm;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.session.SessionManager;
import io.github.lucaseasedup.logit.storage.CacheType;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.storage.Storage.DataType;
import io.github.lucaseasedup.logit.storage.StorageFactory;
import io.github.lucaseasedup.logit.storage.StorageType;
import io.github.lucaseasedup.logit.storage.WrapperStorage;
import io.github.lucaseasedup.logit.util.IoUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * The central part of LogIt.
 */
public final class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    /**
     * Starts up the LogIt core.
     * 
     * @throws IllegalStateException  if the core has already been started.
     * @throws FatalReportedException if critical error occured and LogIt could not start.
     * 
     * @see #isStarted()
     * @see #stop()
     */
    public void start() throws FatalReportedException
    {
        if (isStarted())
            throw new IllegalStateException("The LogIt core has already been started.");
        
        getDataFolder().mkdir();
        getDataFile("lib").mkdir();
        
        firstRun = !getDataFile("config.yml").exists();
        config = new LogItConfiguration();
        
        try
        {
            config.load();
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "Could not load the configuration file.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        catch (InvalidPropertyValueException ex)
        {
            log(Level.SEVERE, "Invalid configuration property value: " + ex.getPropertyName());
            
            FatalReportedException.throwNew(ex);
        }
        
        if (config.getBoolean("logging.file.enabled"))
        {
            openLogFile(config.getString("logging.file.filename"));
        }
        
        if (firstRun)
        {
            getDataFile("backup").mkdir();
            getDataFile("mail").mkdir();
            getDataFile("lang").mkdir();
            
            File passwordRecoveryTemplateFile = getDataFile("mail/password-recovery.html");
            
            if (!passwordRecoveryTemplateFile.exists())
            {
                try
                {
                    IoUtils.extractResource("password-recovery.html", passwordRecoveryTemplateFile);
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, "Could not copy resource: password-recovery.html", ex);
                }
            }
        }
        
        localeManager = new LocaleManager();
        localeManager.registerLocale(EnglishLocale.getInstance());
        localeManager.registerLocale(PolishLocale.getInstance());
        localeManager.registerLocale(GermanLocale.getInstance());
        localeManager.setFallbackLocale(EnglishLocale.class);
        localeManager.switchActiveLocale(getConfig().getString("locale"));
        
        StorageType leadingStorageType = StorageType.decode(
            plugin.getConfig().getString("storage.accounts.leading.storage-type")
        );
        StorageType mirrorStorageType = StorageType.decode(
            plugin.getConfig().getString("storage.accounts.mirror.storage-type")
        );
        
        if (leadingStorageType.equals(StorageType.H2)
                || mirrorStorageType.equals(StorageType.H2))
        {
            LogItPlugin.loadLibrary(LIB_H2);
        }
        
        if (leadingStorageType.equals(StorageType.POSTGRESQL)
                || mirrorStorageType.equals(StorageType.POSTGRESQL))
        {
            LogItPlugin.loadLibrary(LIB_POSTGRESQL);
        }
        
        if (config.getBoolean("password-recovery.enabled"))
        {
            LogItPlugin.loadLibrary(LIB_MAIL);
        }
        
        String accountsUnit = config.getString("storage.accounts.leading.unit");
        AccountKeys accountKeys = new AccountKeys(
            config.getString("storage.accounts.keys.username"),
            config.getString("storage.accounts.keys.salt"),
            config.getString("storage.accounts.keys.password"),
            config.getString("storage.accounts.keys.hashing_algorithm"),
            config.getString("storage.accounts.keys.ip"),
            config.getString("storage.accounts.keys.login_session"),
            config.getString("storage.accounts.keys.email"),
            config.getString("storage.accounts.keys.last_active_date"),
            config.getString("storage.accounts.keys.reg_date"),
            config.getString("storage.accounts.keys.is_locked"),
            config.getString("storage.accounts.keys.persistence")
        );
        Storage leadingAccountStorage = StorageFactory.produceStorage(leadingStorageType,
                config.getConfigurationSection("storage.accounts.leading"));
        Storage mirrorAccountStorage = StorageFactory.produceStorage(mirrorStorageType,
                config.getConfigurationSection("storage.accounts.mirror"));
        CacheType accountCacheType = CacheType.decode(config.getString("storage.accounts.leading.cache"));
        
        @SuppressWarnings("resource")
        WrapperStorage accountStorage = new WrapperStorage.Builder()
                .leading(leadingAccountStorage)
                .keys(accountKeys.getNames())
                .indexKey(accountKeys.username())
                .cacheType(accountCacheType)
                .build();
        accountStorage.mirrorStorage(mirrorAccountStorage, new Hashtable<String, String>()
            {{
                put(config.getString("storage.accounts.leading.unit"),
                    config.getString("storage.accounts.mirror.unit"));
            }});
        
        try
        {
            accountStorage.connect();
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "Could not establish database connection.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        
        try
        {
            accountStorage.createUnit(accountsUnit, accountKeys);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "Could not create accounts table.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        
        try
        {
            accountStorage.setAutobatchEnabled(true);
            
            Hashtable<String, DataType> existingKeys = accountStorage.getKeys(accountsUnit);
            
            for (Map.Entry<String, DataType> e : accountKeys.entrySet())
            {
                if (!existingKeys.containsKey(e.getKey()))
                {
                    accountStorage.addKey(accountsUnit, e.getKey(), e.getValue());
                }
            }
            
            accountStorage.executeBatch();
            accountStorage.clearBatch();
            accountStorage.setAutobatchEnabled(false);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "Could not update accounts table columns.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        
        if (accountCacheType == CacheType.PRELOADED)
        {
            try
            {
                accountStorage.selectEntries(config.getString("storage.accounts.leading.unit"));
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, "Could not preload accounts.", ex);
            }
        }
        
        accountManager = new AccountManager(accountStorage, accountsUnit, accountKeys);
        persistenceManager = new PersistenceManager();
        
        setSerializerEnabled(LocationSerializer.class,
                config.getBoolean("waiting-room.enabled"));
        setSerializerEnabled(AirBarSerializer.class,
                config.getBoolean("force-login.obfuscate-bars.air"));
        setSerializerEnabled(HealthBarSerializer.class,
                config.getBoolean("force-login.obfuscate-bars.health"));
        setSerializerEnabled(ExperienceSerializer.class,
                config.getBoolean("force-login.obfuscate-bars.experience"));
        setSerializerEnabled(HungerBarSerializer.class,
                config.getBoolean("force-login.obfuscate-bars.hunger"));
        
        backupManager = new BackupManager(accountManager);
        sessionManager = new SessionManager();
        
        if (config.getBoolean("password-recovery.enabled"))
        {
            mailSender = new MailSender();
            mailSender.configure(
                config.getString("mail.smtp-host"),
                config.getInt("mail.smtp-port"),
                config.getString("mail.smtp-user"),
                config.getString("mail.smtp-password")
            );
        }
        
        messageDispatcher = new LogItMessageDispatcher();
        
        if (config.getBoolean("profiles.enabled"))
        {
            File profilesPath = getDataFile(config.getString("profiles.path"));
            
            if (!profilesPath.exists())
            {
                profilesPath.mkdir();
            }
            
            profileManager = new ProfileManager(profilesPath,
                    config.getConfigurationSection("profiles.fields"));
        }
        
        accountWatcher = new AccountWatcher();
        
        if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
        {
            vaultPermissions = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
        }
        
        accountManagerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                accountManager, 0, AccountManager.TASK_PERIOD);
        backupManagerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                backupManager, 0, BackupManager.TASK_PERIOD);
        sessionManagerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                sessionManager, 0, SessionManager.TASK_PERIOD);
        accountWatcherTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                accountWatcher, 0, AccountWatcher.TASK_PERIOD);
        
        enableCommands();
        registerEvents();
        
        started = true;
        
        log(Level.FINE, getMessage("PLUGIN_START_SUCCESS"));
        
        if (isFirstRun())
        {
            log(Level.INFO, getMessage("PLUGIN_FIRST_RUN"));
        }
    }
    
    /**
     * Stops the LogIt core.
     * 
     * @throws IllegalStateException if the core has already been stopped.
     * 
     * @see #isStarted()
     * @see #start()
     */
    public void stop()
    {
        if (!isStarted())
            throw new IllegalStateException("The LogIt core is not started.");
        
        disableCommands();
        
        persistenceManager.unregisterSerializer(LocationSerializer.class);
        persistenceManager.unregisterSerializer(AirBarSerializer.class);
        persistenceManager.unregisterSerializer(HealthBarSerializer.class);
        persistenceManager.unregisterSerializer(ExperienceSerializer.class);
        persistenceManager.unregisterSerializer(HungerBarSerializer.class);
        
        try
        {
            accountManager.getStorage().close();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not close database connection.", ex);
        }
        
        Bukkit.getScheduler().cancelTask(accountManagerTaskId);
        Bukkit.getScheduler().cancelTask(sessionManagerTaskId);
        Bukkit.getScheduler().cancelTask(accountWatcherTaskId);
        Bukkit.getScheduler().cancelTask(backupManagerTaskId);
        
        // Unregister all event listeners.
        HandlerList.unregisterAll(plugin);
        
        if (logFileWriter != null)
        {
            try
            {
                logFileWriter.close();
            }
            catch (IOException ex)
            {
                log(Level.WARNING, "Could not close log file.", ex);
            }
        }
        
        started = false;
        
        dispose();
        
        log(Level.FINE, getMessage("PLUGIN_STOP_SUCCESS"));
    }
    
    private void dispose()
    {
        if (isStarted())
            throw new IllegalStateException();
        
        if (config != null)
        {
            config.dispose();
            config = null;
        }
        
        if (localeManager != null)
        {
            localeManager.dispose();
            localeManager = null;
        }
        
        if (accountManager != null)
        {
            accountManager.dispose();
            accountManager = null;
        }
        
        if (persistenceManager != null)
        {
            persistenceManager.dispose();
            persistenceManager = null;
        }
        
        if (backupManager != null)
        {
            backupManager.dispose();
            backupManager = null;
        }
        
        if (sessionManager != null)
        {
            sessionManager.dispose();
            sessionManager = null;
        }
        
        if (mailSender != null)
        {
            mailSender.dispose();
            mailSender = null;
        }
        
        if (messageDispatcher != null)
        {
            messageDispatcher.dispose();
            messageDispatcher = null;
        }
        
        if (profileManager != null)
        {
            profileManager.dispose();
            profileManager = null;
        }
        
        accountWatcher = null;
        vaultPermissions = null;
        
        logFileWriter = null;
    }
    
    /**
     * Restarts the LogIt core.
     * 
     * @throws IllegalStateException  if the LogIt core has not been started yet
     *                                in order to be restarted obviously.
     * @throws FatalReportedException if the LogIt core could not be started again.
     * 
     * @see #isStarted()
     * @see #start()
     */
    public void restart() throws FatalReportedException
    {
        if (!isStarted())
            throw new IllegalStateException("The LogIt core is not started.");
        
        File sessionFile = getDataFile(config.getString("storage.sessions.filename"));
        
        try
        {
            sessionManager.exportSessions(sessionFile);
        }
        catch (IOException ex)
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
            sessionManager.importSessions(sessionFile);
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not import sessions.", ex);
        }
        
        sessionFile.delete();
        
        log(Level.INFO, getMessage("RELOADED"));
    }
    
    /**
     * Checks if a password is equal, after hashing, to {@code hashedPassword}.
     * 
     * <p> If the <i>password.global-hashing-algorithm</i>
     * config setting is set to <i>true</i>,
     * the global hashing algorithm (specified in the config file)
     * will be used instead of the provided {@code hashingAlgorithm}.
     * 
     * @param password         the password to be checked.
     * @param hashedPassword   the hashed password.
     * @param hashingAlgorithm the algorithm used when hashing {@code hashedPassword}.
     * 
     * @return {@code true} if passwords match; {@code false} otherwise.
     * 
     * @see #checkPassword(String, String, String, HashingAlgorithm)
     */
    public boolean checkPassword(String password, String hashedPassword,
                                 HashingAlgorithm hashingAlgorithm)
    {
        if (hashingAlgorithm == null || config.getBoolean("password.global-hashing-algorithm"))
        {
            hashingAlgorithm = getDefaultHashingAlgorithm();
        }
        
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.checkpw(password, hashedPassword);
        }
        else
        {
            return hashedPassword.equals(SecurityHelper.hash(password, hashingAlgorithm));
        }
    }
    
    /**
     * Checks if a password (with a salt appended) is equal,
     * after hashing, to {@code hashedPassword}.
     * 
     * <p> If the <i>password.global-hashing-algorithm</i>
     * config setting is set to <i>true</i>,
     * the global hashing algorithm (specified in the config file)
     * will be used instead of the provided {@code hashingAlgorithm}.
     * 
     * @param password         the password to be checked.
     * @param hashedPassword   the hashed password.
     * @param salt             the salt for the passwords.
     * @param hashingAlgorithm the algorithm used when hashing {@code hashedPassword}.
     * 
     * @return {@code true} if passwords match; {@code false} otherwise.
     * 
     * @see #checkPassword(String, String, HashingAlgorithm)
     */
    public boolean checkPassword(String password,
                                 String hashedPassword,
                                 String salt,
                                 HashingAlgorithm hashingAlgorithm)
    {
        if (hashedPassword == null || hashedPassword.isEmpty())
            return false;
        
        if (hashingAlgorithm == null || config.getBoolean("password.global-hashing-algorithm"))
        {
            hashingAlgorithm = getDefaultHashingAlgorithm();
        }
        
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            try
            {
                return BCrypt.checkpw(password, hashedPassword);
            }
            catch (IllegalArgumentException ex)
            {
                return false;
            }
        }
        else
        {
            if (config.getBoolean("password.use-salt"))
            {
                return hashedPassword.equals(SecurityHelper.hash(password, salt, hashingAlgorithm));
            }
            else
            {
                return hashedPassword.equals(SecurityHelper.hash(password, hashingAlgorithm));
            }
        }
    }
    
    /**
     * Checks if a password is equal, after hashing
     * using the default algorithm, to the global password.
     * 
     * @param password the plain-text password.
     * 
     * @return {@code true} if the passwords match; {@code false} otherwise.
     */
    public boolean checkGlobalPassword(String password)
    {
        return checkPassword(password, config.getString("password.global-password.hash"),
            config.getString("password.global-password.salt"), getDefaultHashingAlgorithm());
    }

    /**
     * Changes the global password.
     * 
     * <p> This method hashes {@code password} with a random salt
     * using the default algorithm specified in the config file.
     * 
     * @param password the new global password.
     */
    public void changeGlobalPassword(String password)
    {
        String salt = SecurityHelper.generateSalt(getDefaultHashingAlgorithm());
        String hash = SecurityHelper.hash(password, salt, getDefaultHashingAlgorithm());
        
        config.set("password.global-password.salt", salt);
        config.set("password.global-password.hash", hash);
        
        log(Level.INFO, getMessage("GLOBALPASS_SET_SUCCESS"));
    }
    
    /**
     * Removes the global password, making it unusable
     * in the login processes following this method call.
     */
    public void removeGlobalPassword()
    {
        config.set("password.global-password.hash", "");
        config.set("password.global-password.salt", "");
        
        log(Level.INFO, getMessage("GLOBALPASS_REMOVE_SUCCESS"));
    }
    
    /**
     * Checks if a player is forced to log in.
     * 
     * <p> Returns {@code true} if the <i>force-login.global</i> config setting
     * is set to <i>true</i>, or the player is in a world with forced login.
     * 
     * <p> If the player name is contained in the <i>force-login.exempt-players</i>
     * config property, it always returns {@code false} regardless of the above conditions.
     * 
     * <p> Note that this method does not check if the player is logged in.
     * For that purpose, use {@link SessionManager#isSessionAlive(Player)}
     * or {@link SessionManager#isSessionAlive(String)}.
     * 
     * @param player the player whom this check will be ran on.
     * 
     * @return {@code true} if the player is forced to log in; {@code false} otherwise.
     */
    public boolean isPlayerForcedToLogIn(Player player)
    {
        String worldName = player.getWorld().getName();
        
        return (config.getBoolean("force-login.global")
                || config.getStringList("force-login.in-worlds").contains(worldName))
             && !containsIgnoreCase(player.getName(),
                     config.getStringList("force-login.exempt-players"));
    }
    
    /**
     * Updates permission groups for a player only if LogIt is linked to Vault.
     * 
     * <p> Permission groups currently supported: <ul>
     *  <li>Registered</li>
     *  <li>Not registered</li>
     *  <li>Logged in</li>
     *  <li>Logged out</li>
     * </ul>
     * 
     * <p> Exact group names will be read from the configuration file.
     * 
     * @param player the player whose permission groups should be updated.
     */
    public void updatePlayerGroup(Player player)
    {
        if (!isLinkedToVault())
            return;
        
        if (accountManager.isRegistered(player.getName()))
        {
            vaultPermissions.playerRemoveGroup(player, config.getString("groups.unregistered"));
            vaultPermissions.playerAddGroup(player, config.getString("groups.registered"));
        }
        else
        {
            vaultPermissions.playerRemoveGroup(player, config.getString("groups.registered"));
            vaultPermissions.playerAddGroup(player, config.getString("groups.unregistered"));
        }
        
        if (sessionManager.isSessionAlive(player))
        {
            vaultPermissions.playerRemoveGroup(player, config.getString("groups.logged-out"));
            vaultPermissions.playerAddGroup(player, config.getString("groups.logged-in"));
        }
        else
        {
            vaultPermissions.playerRemoveGroup(player, config.getString("groups.logged-in"));
            vaultPermissions.playerAddGroup(player, config.getString("groups.logged-out"));
        }
    }
    
    /**
     * Logs a message in the name of LogIt.
     * 
     * <p> The logger message will be saved in a log file if doing so is permitted
     * by the appropriate configuration setting.
     * 
     * @param level   the message level.
     * @param message the message to be logged.
     * 
     * @see #log(Level, String, Throwable)
     */
    public void log(Level level, String message)
    {
        if (level == null)
            throw new IllegalArgumentException();
        
        if (config != null && config.isLoaded())
        {
            if (config.getBoolean("logging.file.enabled")
                    && level.intValue() >= config.getInt("logging.file.level"))
            {
                if (logFileWriter == null)
                {
                    openLogFile(config.getString("logging.file.filename"));
                }
                
                try
                {
                    logFileWriter.write(logDateFormat.format(new Date()));
                    logFileWriter.write(" [");
                    logFileWriter.write(level.getName());
                    logFileWriter.write("] ");
                    logFileWriter.write(ChatColor.stripColor(message));
                    logFileWriter.write("\n");
                }
                catch (IOException ex)
                {
                    plugin.getLogger().log(Level.WARNING, "Could not log to file.", ex);
                }
            }
            
            if (config.getBoolean("logging.verbose-console"))
            {
                System.out.println("[" + level + "] " + ChatColor.stripColor(message));
                
                return;
            }
        }
        
        plugin.getLogger().log(level, ChatColor.stripColor(message));
    }
    
    /**
     * Logs a message with a {@code Throwable} in the name of LogIt.
     * 
     * <p> The logger message will be saved in a log file if doing so is permitted
     * by the appropriate configuration setting.
     * 
     * @param level     the message level.
     * @param message   the message to be logged.
     * @param throwable the throwable whose stack trace should be appended to the log.
     * 
     * @see #log(Level, String)
     */
    public void log(Level level, String message, Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        
        try (PrintWriter pw = new PrintWriter(sw))
        {
            throwable.printStackTrace(pw);
        }
        
        log(level, message + " [Exception stack trace:\n" + sw.toString() + "]");
    }
    
    /**
     * Logs a {@code Throwable} in the name of LogIt.
     * 
     * <p> The logger message will be saved in a log file if doing so is permitted
     * by the appropriate configuration setting.
     * 
     * @param level     the logging level.
     * @param throwable the throwable to be logged.
     * 
     * @see #log(Level, String, Throwable)
     */
    public void log(Level level, Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        
        try (PrintWriter pw = new PrintWriter(sw))
        {
            throwable.printStackTrace(pw);
        }
        
        log(level, "Caught exception:\n" + sw.toString());
    }
    
    private void openLogFile(String filename)
    {
        File logFile = getDataFile(filename);
        
        if (logFile.length() > 300000)
        {
            int suffix = 0;
            File nextLogFile;
            
            do
            {
                suffix++;
                nextLogFile = getDataFile(filename + "." + suffix);
            }
            while (nextLogFile.exists());
            
            logFile.renameTo(nextLogFile);
        }
        
        try
        {
            logFileWriter = new FileWriter(logFile, true);
        }
        catch (IOException ex)
        {
            plugin.getLogger().log(Level.WARNING, "Could not open log file for writing.", ex);
        }
    }
    
    private void setSerializerEnabled(Class<? extends PersistenceSerializer> clazz, boolean status)
            throws FatalReportedException
    {
        if (status)
        {
            try
            {
                persistenceManager.registerSerializer(clazz);
            }
            catch (ReflectiveOperationException ex)
            {
                log(Level.SEVERE,
                        "Could not register persistence serializer: " + clazz.getSimpleName(), ex);
                
                FatalReportedException.throwNew(ex);
            }
        }
        else for (Player player : Bukkit.getOnlinePlayers())
        {
            try
            {
                persistenceManager.unserializeUsing(player, clazz);
            }
            catch (ReflectiveOperationException | IOException ex)
            {
                log(Level.WARNING,
                        "Could not unserialize persistence for player: " + player.getName(), ex);
            }
        }
    }
    
    private void enableCommands()
    {
        enableCommand("login", new LoginCommand());
        enableCommand("logout", new LogoutCommand());
        enableCommand("remember", new RememberCommand(),
                config.getBoolean("login-sessions.enabled"));
        enableCommand("register", new RegisterCommand());
        enableCommand("unregister", new UnregisterCommand());
        enableCommand("changepass", new ChangePassCommand(),
                !config.getBoolean("password.disable-passwords"));
        enableCommand("changeemail", new ChangeEmailCommand());
        enableCommand("recoverpass", new RecoverPassCommand(),
                config.getBoolean("password-recovery.enabled"));
        enableCommand("profile", new ProfileCommand(), config.getBoolean("profiles.enabled"));
        enableCommand("acclock", new AcclockCommand());
        enableCommand("accunlock", new AccunlockCommand());
        enableCommand("$logit-nop-command", new NopCommandExecutor());
    }
    
    private void disableCommands()
    {
        disableCommand("login");
        disableCommand("logout");
        disableCommand("remember");
        disableCommand("register");
        disableCommand("unregister");
        disableCommand("changepass");
        disableCommand("changeemail");
        disableCommand("recoverpass");
        disableCommand("profile");
        disableCommand("acclock");
        disableCommand("accunlock");
        disableCommand("$logit-nop-command");
    }
    
    private void disableCommand(String command)
    {
        plugin.getCommand(command).setExecutor(new DisabledCommandExecutor());
    }
    
    private void enableCommand(String command, CommandExecutor executor, boolean enabled)
    {
        if (enabled)
        {
            plugin.getCommand(command).setExecutor(executor);
        }
        else
        {
            disableCommand(command);
        }
    }
    
    private void enableCommand(String command, CommandExecutor executor)
    {
        enableCommand(command, executor, true);
    }
    
    private void registerEvents()
    {
        plugin.getServer().getPluginManager().registerEvents(messageDispatcher, plugin);
        plugin.getServer().getPluginManager().registerEvents(new ServerEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SessionEventListener(), plugin);
    }
    
    /**
     * Returns the LogIt plugin object.
     * 
     * <p> Most of times, all the work will be done with the LogIt core,
     * but the {@code LogItPlugin} may come useful if you want to,
     * for example, reload the message files or load external libraries.
     * 
     * @return the LogIt plugin object.
     */
    public LogItPlugin getPlugin()
    {
        return plugin;
    }
    
    /**
     * Returns the LogIt data folder as a {@code File} object (<i>/plugins/LogIt/</i>).
     * 
     * @return the data folder.
     */
    public File getDataFolder()
    {
        return plugin.getDataFolder();
    }
    
    /**
     * Returns a file, as a {@code File} object,
     * relative to the LogIt data folder (<i>/plugins/LogIt/</i>).
     * 
     * @param path the relative path.
     * 
     * @return the data file.
     */
    public File getDataFile(String path)
    {
        return new File(getDataFolder(), path);
    }
    
    /**
     * Checks if this is the first time LogIt is running on this server.
     * 
     * @return {@code true} if LogIt is running for the first time;
     *         {@code false} otherwise.
     */
    public boolean isFirstRun()
    {
        return firstRun;
    }
    
    /**
     * Checks if the LogIt core has been successfully started and is running.
     * 
     * @return {@code true} if the LogIt core is started; {@code false} otherwise.
     */
    public boolean isStarted()
    {
        return started;
    }
    
    public LogItConfiguration getConfig()
    {
        return config;
    }
    
    /**
     * Checks if the LogIt configuration file has been successfully loaded.
     * 
     * @return {@code true} if the configuration file has been loaded;
     *         {@code false} otherwise.
     */
    public boolean isConfigLoaded()
    {
        return config != null && config.isLoaded();
    }
    
    /**
     * Returns the default hashing algorithm specified in the config file.
     * 
     * @return the default hashing algorithm.
     */
    public HashingAlgorithm getDefaultHashingAlgorithm()
    {
        return HashingAlgorithm.decode(plugin.getConfig().getString("password.hashing-algorithm"));
    }
    
    public IntegrationType getIntegration()
    {
        return IntegrationType.decode(plugin.getConfig().getString("integration"));
    }
    
    public LocaleManager getLocaleManager()
    {
        return localeManager;
    }
    
    public AccountManager getAccountManager()
    {
        return accountManager;
    }
    
    public PersistenceManager getPersistenceManager()
    {
        return persistenceManager;
    }
    
    public BackupManager getBackupManager()
    {
        return backupManager;
    }
    
    public SessionManager getSessionManager()
    {
        return sessionManager;
    }

    public MailSender getMailSender()
    {
        return mailSender;
    }
    
    public LogItMessageDispatcher getMessageDispatcher()
    {
        return messageDispatcher;
    }
    
    public ProfileManager getProfileManager()
    {
        return profileManager;
    }
    
    /**
     * Checks if LogIt is linked to the Vault plugin
     * (e.i. Vault is enabled on this server and LogIt has successfully
     * obtained the Vault permission provider when it was starting up).
     * 
     * @return {@code true} if LogIt is linked to Vault; {@code false} otherwise.
     */
    public boolean isLinkedToVault()
    {
        return vaultPermissions != null;
    }
    
    /**
     * The preferred way to obtain the instance of {@code LogItCore}.
     * 
     * @return the instance of {@code LogItCore}.
     */
    public static LogItCore getInstance()
    {
        if (instance == null)
        {
            instance = new LogItCore(LogItPlugin.getInstance());
        }
        
        return instance;
    }
    
    /**
     * INTERNAL is a message level providing internal information typically used for debugging.
     */
    public static final Level INTERNAL = new CustomLevel("INTERNAL", -1000);
    
    /**
     * The filename of H2 library found in the /lib directory.
     */
    public static final String LIB_H2 = "h2small-1.3.171.jar";
    
    /**
     * The filename of PostgreSQL library found in the /lib directory.
     */
    public static final String LIB_POSTGRESQL = "postgresql-9.3-1100.jdbc4.jar";
    
    /**
     * The filename of JavaMail library found in the /lib directory.
     */
    public static final String LIB_MAIL = "mail-1.4.7.jar";
    
    private static volatile LogItCore instance = null;
    
    private final LogItPlugin plugin;
    private boolean firstRun;
    private boolean started = false;
    
    private LogItConfiguration     config;
    private LocaleManager          localeManager;
    private AccountManager         accountManager;
    private PersistenceManager     persistenceManager;
    private BackupManager          backupManager;
    private SessionManager         sessionManager;
    private MailSender             mailSender;
    private LogItMessageDispatcher messageDispatcher;
    private ProfileManager         profileManager;
    
    private AccountWatcher  accountWatcher;
    private Permission      vaultPermissions;
    
    private int accountManagerTaskId;
    private int backupManagerTaskId;
    private int sessionManagerTaskId;
    private int accountWatcherTaskId;
    
    private FileWriter logFileWriter;
    private final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
