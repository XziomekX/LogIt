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
import io.github.lucaseasedup.logit.command.NopCommandExecutor;
import io.github.lucaseasedup.logit.command.ProfileCommand;
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
import io.github.lucaseasedup.logit.listener.AccountEventListener;
import io.github.lucaseasedup.logit.listener.BlockEventListener;
import io.github.lucaseasedup.logit.listener.EntityEventListener;
import io.github.lucaseasedup.logit.listener.InventoryEventListener;
import io.github.lucaseasedup.logit.listener.PlayerEventListener;
import io.github.lucaseasedup.logit.listener.ServerEventListener;
import io.github.lucaseasedup.logit.listener.SessionEventListener;
import io.github.lucaseasedup.logit.listener.TickEventListener;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.persistence.AirBarSerializer;
import io.github.lucaseasedup.logit.persistence.ExperienceSerializer;
import io.github.lucaseasedup.logit.persistence.HealthBarSerializer;
import io.github.lucaseasedup.logit.persistence.HungerBarSerializer;
import io.github.lucaseasedup.logit.persistence.InventorySerializer;
import io.github.lucaseasedup.logit.persistence.LocationSerializer;
import io.github.lucaseasedup.logit.persistence.PersistenceManager;
import io.github.lucaseasedup.logit.persistence.PersistenceSerializer;
import io.github.lucaseasedup.logit.profile.ProfileManager;
import io.github.lucaseasedup.logit.session.SessionManager;
import io.github.lucaseasedup.logit.util.FileUtils;
import io.github.lucaseasedup.logit.util.IoUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * This class is the central part of LogIt.
 */
public final class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.firstRun = !getDataFile("config.yml").exists();
    }
    
    /**
     * Starts up the LogItCore.
     * 
     * <p> Does nothing if it has already been started.
     * 
     * @throws FatalReportedException if critical error occured and LogIt could not start.
     */
    public void start() throws FatalReportedException
    {
        if (started)
            return;
        
        getDataFile("lib").mkdir();
        
        config = new LogItConfiguration();
        
        try
        {
            config.load();
        }
        catch (IOException ex)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not load the configuration file.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        
        if (config.getBoolean("log-to-file.enabled"))
        {
            File logFile = getDataFile(config.getString("log-to-file.filename"));
            
            try
            {
                logFileWriter = new FileWriter(logFile, true);
            }
            catch (IOException ex)
            {
                plugin.getLogger().log(Level.WARNING, "Could not open log file for writing.", ex);
            }
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
                    FileUtils.extractResource("/password-recovery.html",
                            passwordRecoveryTemplateFile);
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, "Could not copy resource password-recovery.html.", ex);
                }
            }
        }
        
        HashingAlgorithm defaultHashingAlgorithm = getDefaultHashingAlgorithm();
        
        if (defaultHashingAlgorithm.equals(HashingAlgorithm.UNKNOWN))
        {
            log(Level.SEVERE, getMessage("UNKNOWN_HASHING_ALGORITHM")
                    .replace("%ha%", defaultHashingAlgorithm.name()));
            
            FatalReportedException.throwNew();
        }
        
        StorageType storageType =
                StorageType.decode(plugin.getConfig().getString("storage.accounts.db-type"));
        
        try
        {
            ReportedException.incrementRequestCount();
            
            if (storageType.equals(StorageType.H2))
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
            ex.rethrowAsFatal();
        }
        finally
        {
            ReportedException.decrementRequestCount();
        }
        
        try
        {
            switch (storageType)
            {
                case SQLITE:
                {
                    database = new SqliteDatabase("jdbc:sqlite:" + dataFolder
                            + "/" + config.getString("storage.accounts.sqlite.filename"));
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
                    database = new H2Database("jdbc:h2:" + dataFolder
                            + "/" + config.getString("storage.accounts.h2.filename"));
                    database.connect();
                    
                    break;
                }
                case CSV:
                {
                    database = new CsvDatabase(dataFolder);
                    database.connect();
                    
                    break;
                }
                default:
                {
                    log(Level.SEVERE, getMessage("UNKNOWN_STORAGE_TYPE")
                            .replace("%st%", storageType.name()));
                    
                    FatalReportedException.throwNew();
                }
            }
        }
        catch (SQLException ex)
        {
            log(Level.SEVERE, "Could not open database connection.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        
        pinger = new Pinger(database);
        accountTable = new Table(database, config.getString("storage.accounts.table"),
                config.getConfigurationSection("storage.accounts.columns"));
        
        if (accountTable.isColumnDisabled("logit.accounts.username"))
        {
            log(Level.SEVERE, "Username column must not be disabled.");
            
            FatalReportedException.throwNew();
        }
        
        try
        {
            accountTable.open();
        }
        catch (SQLException ex)
        {
            log(Level.SEVERE, "Could not open account table.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        
        accountManager = new AccountManager(accountTable);
        
        try
        {
            ReportedException.incrementRequestCount();
            
            accountManager.loadAccounts();
        }
        catch (ReportedException ex)
        {
            ex.rethrowAsFatal();
        }
        finally
        {
            ReportedException.decrementRequestCount();
        }
        
        if (getConfig().getBoolean("profiles.enabled"))
        {
            File profilesPath = getDataFile(getConfig().getString("profiles.path"));
            
            if (!profilesPath.exists())
            {
                profilesPath.mkdir();
            }
            
            profileManager = new ProfileManager(profilesPath,
                    config.getConfigurationSection("profiles.fields"));
        }
        
        persistenceManager = new PersistenceManager();
        
        setSerializerEnabled(LocationSerializer.class,
                getConfig().getBoolean("waiting-room.enabled"));
        setSerializerEnabled(AirBarSerializer.class,
                getConfig().getBoolean("force-login.obfuscate-bars.air"));
        setSerializerEnabled(HealthBarSerializer.class,
                getConfig().getBoolean("force-login.obfuscate-bars.health"));
        setSerializerEnabled(ExperienceSerializer.class,
                getConfig().getBoolean("force-login.obfuscate-bars.experience"));
        setSerializerEnabled(HungerBarSerializer.class,
                getConfig().getBoolean("force-login.obfuscate-bars.hunger"));
        setSerializerEnabled(InventorySerializer.class,
                getConfig().getBoolean("force-login.hide-inventory"));
        
        accountWatcher = new AccountWatcher();
        backupManager  = new BackupManager();
        sessionManager = new SessionManager();
        tickEventCaller = new TickEventCaller();
        
        if (config.getBoolean("password-recovery.enabled")
                && !accountTable.isColumnDisabled("logit.accounts.email"))
        {
            mailSender = new MailSender();
            mailSender.configure(config.getString("mail.smtp-host"), config.getInt("mail.smtp-port"),
                config.getString("mail.smtp-user"), config.getString("mail.smtp-password"));
        }
        
        pingerTaskId =
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, pinger, 0L, 2400L);
        sessionManagerTaskId =
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, sessionManager, 0L, 20L);
        tickEventCallerTaskId =
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, tickEventCaller, 0L, 1L);
        accountWatcherTaskId =
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, accountWatcher, 0L, 12000L);
        backupManagerTaskId =
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, backupManager, 0L, 40L);
        
        if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
        {
            vaultPermissions =
                    Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
        }
        
        registerEvents();
        setCommandExecutors();
        
        started = true;
        
        log(Level.FINE, getMessage("PLUGIN_START_SUCCESS")
                .replace("%st%", storageType.name()));
        
        if (firstRun)
        {
            log(Level.INFO, getMessage("PLUGIN_FIRST_RUN"));
        }
    }
    
    /**
     * Stops the LogItCore.
     * 
     * <p> Does nothing if it has not been started.
     */
    public void stop()
    {
        if (!started)
            return;
        
        persistenceManager.unregisterSerializer(LocationSerializer.class);
        persistenceManager.unregisterSerializer(AirBarSerializer.class);
        persistenceManager.unregisterSerializer(HealthBarSerializer.class);
        persistenceManager.unregisterSerializer(ExperienceSerializer.class);
        persistenceManager.unregisterSerializer(HungerBarSerializer.class);
        persistenceManager.unregisterSerializer(InventorySerializer.class);
        
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
        
        // Unregister all event listeners.
        HandlerList.unregisterAll(getPlugin());
        
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
            
            logFileWriter = null;
        }
        
        started = false;
        
        log(Level.FINE, getMessage("PLUGIN_STOP_SUCCESS"));
    }
    
    /**
     * Restarts the LogItCore by invoking {@link #stop} and {@link #start}.
     * 
     * @throws FatalReportedException if LogItCore could not be started again.
     */
    public void restart() throws FatalReportedException
    {
        File sessionFile = getDataFile(config.getString("storage.sessions.filename"));
        
        try
        {
            sessionManager.exportSessions(sessionFile);
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
            sessionManager.importSessions(sessionFile);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not import sessions.", ex);
        }
        
        sessionFile.delete();
        
        log(Level.INFO, getMessage("RELOADED"));
    }
    
    /**
     * Checks if a plain-text password is equal, after hashing, to {@code hashedPassword}.
     * 
     * @param password         the plain-text password.
     * @param hashedPassword   the hashed password.
     * @param hashingAlgorithm the algorithm used when hashing {@code hashedPassword}.
     * @return {@code true} if passwords match; {@code false} otherwise.
     */
    public boolean checkPassword(String password, String hashedPassword,
                                 HashingAlgorithm hashingAlgorithm)
    {
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.checkpw(password, hashedPassword);
        }
        else
        {
            return hashedPassword.equals(hash(password, hashingAlgorithm));
        }
    }
    
    /**
     * Checks if a plain-text password with a salt appended
     * is equal, after hashing, to {@code hashedPassword}.
     * 
     * @param password         the plain-text password.
     * @param hashedPassword   the hashed password.
     * @param salt             the salt to be appended to {@code password}.
     * @param hashingAlgorithm the algorithm used when hashing {@code hashedPassword}.
     * @return {@code true} if passwords match; {@code false} otherwise.
     */
    public boolean checkPassword(String password,
                                 String hashedPassword,
                                 String salt,
                                 HashingAlgorithm hashingAlgorithm)
    {
        if (hashedPassword == null)
            return false;
        
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            return BCrypt.checkpw(password, hashedPassword);
        }
        else
        {
            return hashedPassword.equals(hash(password, salt, hashingAlgorithm));
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
     * <p> The password will be hashed using the default hashing algorithm.
     * 
     * <p> A salt is automatically generated and appended to {@code password}. 
     * 
     * @param password the new global password.
     */
    public void changeGlobalPassword(String password)
    {
        String salt = HashGenerator.generateSalt(getDefaultHashingAlgorithm());
        
        config.set("password.global-password.salt", salt);
        config.set("password.global-password.hash",
                hash(password, salt, getDefaultHashingAlgorithm()));
        
        log(Level.INFO, getMessage("GLOBALPASS_SET_SUCCESS"));
    }
    
    public void removeGlobalPassword()
    {
        config.set("password.global-password.hash", "");
        config.set("password.global-password.salt", "");
        
        log(Level.INFO, getMessage("GLOBALPASS_REMOVE_SUCCESS"));
    }
    
    /**
     * Changes a player's password with a randomly generated one,
     * and sends it to the player's e-mail address.
     * 
     * @param username a username of the player whom the mail will be sent.
     */
    public void recoverPassword(String username)
    {
        try
        {
            ReportedException.incrementRequestCount();
            
            if (mailSender == null)
                throw new RuntimeException("MailSender not initialized.");
            
            String to = accountManager.getEmail(username);
            String from = config.getString("mail.email-address");
            String subject = parseMessage(config.getString("password-recovery.subject"), new String[]{
                "%player%", username,
            });
            
            String newPassword = generatePassword(config.getInt("password-recovery.password-length"),
                config.getString("password-recovery.password-combination"));
            accountManager.changeAccountPassword(username, newPassword);
            
            File bodyTemplateFile = getDataFile(config.getString("password-recovery.body-template"));
            String bodyTemplate;
            
            try (InputStream bodyTemplateInputStream = new FileInputStream(bodyTemplateFile))
            {
                bodyTemplate = IoUtils.toString(bodyTemplateInputStream);
            }
            
            String body = parseMessage(bodyTemplate, new String[]{
                "%player%", username,
                "%password%", newPassword
            });
            
            mailSender.sendMail(new String[]{to}, from, subject, body,
                    config.getBoolean("password-recovery.html-enabled"));
            
            log(Level.FINE, getMessage("RECOVER_PASSWORD_SUCCESS_LOG", new String[]{
                "%player%", username,
                "%email%", to,
            }));
        }
        catch (ReportedException | IOException ex)
        {
            log(Level.WARNING, getMessage("RECOVER_PASSWORD_FAIL_LOG", new String[]{
                "%player%", username,
            }), ex);
            
            ReportedException.throwNew(ex);
        }
        finally
        {
            ReportedException.decrementRequestCount();
        }
    }
    
    /**
     * Generates a random password of length equal to {@code length},
     * consisting only of the characters contained in {@code combination}.
     * 
     * @param length      the desired password length.
     * @param combination the letterset used in the generation process.
     * @return the generated password.
     */
    public String generatePassword(int length, String combination)
    {
        char[] charArray = combination.toCharArray();
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        
        for (int i = 0, n = charArray.length; i < length; i++)
        {
            sb.append(charArray[random.nextInt(n)]);
        }
        
        return sb.toString();
    }
    
    /**
     * Checks if a player is forced to log in.
     * 
     * <p> Returns {@code true} if <i>"force-login.global"</i> is set to <i>true</i>,
     * or the player is in a world with forced login; {@code false} otherwise.
     * 
     * <p> If the player has the <i>"logit.force-login.exempt"</i> permission,
     * it always returns {@code false}.
     * 
     * <p> Note that this method does not check if the player is logged in.
     * For that purpose, use {@link SessionManager#isSessionAlive(Player)}
     * or {@link SessionManager#isSessionAlive(String)}.
     * 
     * @param player the player whom this check will be ran on.
     * @return {@code true} if the player is forced to log in; {@code false} otherwise.
     */
    public boolean isPlayerForcedToLogIn(Player player)
    {
        String worldName = player.getWorld().getName();
        
        return (config.getBoolean("force-login.global")
             || config.getStringList("force-login.in-worlds").contains(worldName))
             && !player.hasPermission("logit.force-login.exempt");
    }
    
    /**
     * Sends a message to the specified player telling them to either log in or register.
     * 
     * @param player the player to whom the message will be sent.
     */
    public void sendForceLoginMessage(Player player)
    {
        if (accountManager.isRegistered(player.getName()))
        {
            if (!accountManager.getTable().isColumnDisabled("logit.accounts.password"))
            {
                player.sendMessage(getMessage("PLEASE_LOGIN"));
            }
            else
            {
                player.sendMessage(getMessage("PLEASE_LOGIN_NOPASS"));
            }
        }
        else
        {
            if (!accountManager.getTable().isColumnDisabled("logit.accounts.password"))
            {
                player.sendMessage(getMessage("PLEASE_REGISTER"));
            }
            else
            {
                player.sendMessage(getMessage("PLEASE_REGISTER_NOPASS"));
            }
        }
    }
    
    /**
     * Updates permission groups for a player if LogIt is linked to Vault.
     * 
     * <p> Permission groups currently supported: <ul>
     *  <li>Registered</li>
     *  <li>Not registered</li>
     *  <li>Logged in</li>
     *  <li>Logged out</li>
     * </ul>
     * 
     * <p> Exact group names are taken from the LogIt configuration file.
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
     * Checks if LogIt is linked to Vault.
     * 
     * @return {@code true} if LogIt is linked to Vault; {@code false} otherwise.
     */
    public boolean isLinkedToVault()
    {
        return vaultPermissions != null;
    }
    
    /**
     * Hashes a string using the specified algorithm.
     * 
     * @param string           the string to be hashed.
     * @param hashingAlgorithm the hashing algorithm to be used.
     * 
     * @return the resulting hash.
     * 
     * @throws IllegalArgumentException if this method does not support the given algorithm.
     */
    public String hash(String string, HashingAlgorithm hashingAlgorithm)
    {
        switch (hashingAlgorithm)
        {
            case PLAIN:
                return string;
                
            case MD2:
                return getMd2(string);
                
            case MD5:
                return getMd5(string);
                
            case SHA1:
                return getSha1(string);
                
            case SHA256:
                return getSha256(string);
                
            case SHA384:
                return getSha384(string);
                
            case SHA512:
                return getSha512(string);
                
            case WHIRLPOOL:
                return getWhirlpool(string);
                
            case BCRYPT:
                return getBCrypt(string, "");
                
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + hashingAlgorithm.toString());
        }
    }
    
    /**
     * Hashes a string with a salt using the specified algorithm.
     * 
     * @param string           the string to be hashed.
     * @param salt             the salt to be appended to {@code string}.
     * @param hashingAlgorithm the hashing algorithm to be used.
     * @return resulting hash.
     */
    public String hash(String string, String salt, HashingAlgorithm hashingAlgorithm)
    {
        String hash;
        
        if (hashingAlgorithm == HashingAlgorithm.BCRYPT)
        {
            hash = getBCrypt(string, salt);
        }
        else if (hashingAlgorithm == HashingAlgorithm.PLAIN)
        {
            hash = hash(string, hashingAlgorithm);
        }
        else
        {
            hash = hash(string + salt, hashingAlgorithm);
        }
        
        return hash;
    }
    
    public HashingAlgorithm getDefaultHashingAlgorithm()
    {
        return HashingAlgorithm.decode(plugin.getConfig().getString("password.hashing-algorithm"));
    }
    
    public IntegrationType getIntegration()
    {
        return IntegrationType.decode(plugin.getConfig().getString("integration"));
    }
    
    /**
     * Logs a message in the name of LogIt.
     * 
     * @param level   message level.
     * @param message message.
     */
    public void log(Level level, String message)
    {
        if (config.getBoolean("log-to-file.enabled") && logFileWriter != null)
        {
            try
            {
                logFileWriter.write(logDateFormat.format(new Date()));
                logFileWriter.write(" [");
                logFileWriter.write(level.getName());
                logFileWriter.write("] ");
                logFileWriter.write(stripColor(message));
                logFileWriter.write("\n");
            }
            catch (IOException ex)
            {
                plugin.getLogger().log(Level.WARNING, "Could not log to a file.", ex);
            }
        }
        
        plugin.getLogger().log(level, stripColor(message));
    }
    
    public void log(Level level, String message, Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        
        try (PrintWriter pw = new PrintWriter(sw))
        {
            throwable.printStackTrace(pw);
        }
        
        log(level, message + " [Exception stack trace:\n" + sw.toString() + "]");
    }
    
    public boolean isConfigLoaded()
    {
        return config.isLoaded();
    }
    
    public PersistenceManager getPersistenceManager()
    {
        return persistenceManager;
    }
    
    public ProfileManager getProfileManager()
    {
        return profileManager;
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
    
    protected File getDataFile(String path)
    {
        return new File(dataFolder, path);
    }
    
    protected Permission getVaultPermissions()
    {
        return vaultPermissions;
    }
    
    private void setCommandExecutors()
    {
        setCommandExecutor("logit", new LogItCommand(), true);
        setCommandExecutor("logit", new LogItCommand(), true);
        setCommandExecutor("login", new LoginCommand(), true);
        setCommandExecutor("logout", new LogoutCommand(), true);
        setCommandExecutor("register", new RegisterCommand(), true);
        setCommandExecutor("unregister", new UnregisterCommand(), true);
        setCommandExecutor("changepass", new ChangePassCommand(),
                !accountTable.isColumnDisabled("logit.accounts.password"));
        setCommandExecutor("changeemail", new ChangeEmailCommand(),
                !accountTable.isColumnDisabled("logit.accounts.email"));
        setCommandExecutor("recoverpass", new RecoverPassCommand(),
                !accountTable.isColumnDisabled("logit.accounts.email")
                && config.getBoolean("password-recovery.enabled"));
        setCommandExecutor("profile", new ProfileCommand(), config.getBoolean("profiles.enabled"));
        setCommandExecutor("$logit-nop-command", new NopCommandExecutor(), true);
    }
    
    private void setCommandExecutor(String command, CommandExecutor executor, boolean enabled)
    {
        if (enabled)
        {
            plugin.getCommand(command).setExecutor(executor);
        }
        else
        {
            plugin.getCommand(command).setExecutor(new DisabledCommandExecutor());
        }
    }
    
    private void registerEvents()
    {
        plugin.getServer().getPluginManager().registerEvents(new TickEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ServerEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AccountEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SessionEventListener(), plugin);
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
        else
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                try
                {
                    persistenceManager.unserializeUsing(player, clazz);
                }
                catch (ReflectiveOperationException | SQLException ex)
                {
                    log(Level.WARNING,
                            "Could not unserialize persistence for player: " + player.getName(), ex);
                }
            }
        }
    }
    
    /**
     * The preferred way to obtain the instance of LogItCore.
     * 
     * @return the instance of LogItCore.
     */
    public static LogItCore getInstance()
    {
        if (instance == null)
        {
            instance = new LogItCore(LogItPlugin.getInstance());
        }
        
        return instance;
    }
    
    public static enum StorageType
    {
        UNKNOWN, SQLITE, MYSQL, H2, CSV;
        
        public static StorageType decode(String s)
        {
            switch (s.toLowerCase())
            {
            case "sqlite": return SQLITE;
            case "mysql":  return MYSQL;
            case "h2":     return H2;
            case "csv":    return CSV;
            default:       return UNKNOWN;
            }
        }
    }
    
    public static enum HashingAlgorithm
    {
        UNKNOWN, PLAIN, MD2, MD5, SHA1, SHA256, SHA384, SHA512, WHIRLPOOL, BCRYPT;
        
        public static HashingAlgorithm decode(String s)
        {
            switch (s.toLowerCase())
            {
            case "plain":     return PLAIN;
            case "md2":       return MD2;
            case "md5":       return MD5;
            case "sha-1":     return SHA1;
            case "sha-256":   return SHA256;
            case "sha-384":   return SHA384;
            case "sha-512":   return SHA512;
            case "whirlpool": return WHIRLPOOL;
            case "bcrypt":    return BCRYPT;
            default:          return UNKNOWN;
            }
        }
        
        /**
         * Converts this {@code HashingAlgorithm} to a string representation.
         * 
         * @return the string representation of this {@code HashingAlgorithm},
         *         or {@code null} if no representation for this
         *         {@code HashingAlgorithm} was implemented.
         */
        public String encode()
        {
            switch (this)
            {
            case PLAIN:     return "plain";
            case MD2:       return "md2";
            case MD5:       return "md5";
            case SHA1:      return "sha-1";
            case SHA256:    return "sha-256";
            case SHA384:    return "sha-384";
            case SHA512:    return "sha-512";
            case WHIRLPOOL: return "whirlpool";
            case BCRYPT:    return "bcrypt";
            default:        return null;
            }
        }
    }
    
    public static enum IntegrationType
    {
        UNKNOWN, NONE, PHPBB2;
        
        public static IntegrationType decode(String s)
        {
            switch (s.toLowerCase())
            {
            case "none":   return NONE;
            case "phpbb2": return PHPBB2;
            default:       return UNKNOWN;
            }
        }
    }
    
    public static final String LIB_H2 = "h2small-1.3.171.jar";
    public static final String LIB_MAIL = "mail-1.4.7.jar";
    
    private static LogItCore instance = null;
    
    private final LogItPlugin plugin;
    private final File dataFolder;
    
    private final boolean firstRun;
    private boolean started = false;
    
    private FileWriter logFileWriter;
    private final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private LogItConfiguration  config;
    private Database            database;
    private Table               accountTable;
    private Pinger              pinger;
    private Permission          vaultPermissions;
    private SessionManager      sessionManager;
    private AccountManager      accountManager;
    private AccountWatcher      accountWatcher;
    private BackupManager       backupManager;
    private PersistenceManager  persistenceManager;
    private ProfileManager      profileManager;
    private MailSender          mailSender;
    private TickEventCaller     tickEventCaller;
    
    private int pingerTaskId;
    private int sessionManagerTaskId;
    private int tickEventCallerTaskId;
    private int accountWatcherTaskId;
    private int backupManagerTaskId;
}
