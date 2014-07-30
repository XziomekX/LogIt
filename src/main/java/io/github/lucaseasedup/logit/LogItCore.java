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

import static io.github.lucaseasedup.logit.util.CollectionUtils.containsIgnoreCase;
import static io.github.lucaseasedup.logit.util.MessageHelper._;
import com.google.common.io.Files;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.account.AccountWatcher;
import io.github.lucaseasedup.logit.backup.BackupManager;
import io.github.lucaseasedup.logit.command.AcclockCommand;
import io.github.lucaseasedup.logit.command.AccunlockCommand;
import io.github.lucaseasedup.logit.command.ChangeEmailCommand;
import io.github.lucaseasedup.logit.command.ChangePassCommand;
import io.github.lucaseasedup.logit.command.DisabledCommandExecutor;
import io.github.lucaseasedup.logit.command.LogItTabCompleter;
import io.github.lucaseasedup.logit.command.LoginCommand;
import io.github.lucaseasedup.logit.command.LoginHistoryCommand;
import io.github.lucaseasedup.logit.command.LogoutCommand;
import io.github.lucaseasedup.logit.command.NopCommandExecutor;
import io.github.lucaseasedup.logit.command.ProfileCommand;
import io.github.lucaseasedup.logit.command.RecoverPassCommand;
import io.github.lucaseasedup.logit.command.RegisterCommand;
import io.github.lucaseasedup.logit.command.RememberCommand;
import io.github.lucaseasedup.logit.command.UnregisterCommand;
import io.github.lucaseasedup.logit.config.ConfigurationManager;
import io.github.lucaseasedup.logit.config.InvalidPropertyValueException;
import io.github.lucaseasedup.logit.config.PredefinedConfiguration;
import io.github.lucaseasedup.logit.cooldown.CooldownManager;
import io.github.lucaseasedup.logit.craftreflect.CraftReflect;
import io.github.lucaseasedup.logit.hooks.VaultHook;
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
import io.github.lucaseasedup.logit.logging.LogItCoreLogger;
import io.github.lucaseasedup.logit.persistence.AirBarSerializer;
import io.github.lucaseasedup.logit.persistence.ExperienceSerializer;
import io.github.lucaseasedup.logit.persistence.HealthBarSerializer;
import io.github.lucaseasedup.logit.persistence.HungerBarSerializer;
import io.github.lucaseasedup.logit.persistence.LocationSerializer;
import io.github.lucaseasedup.logit.persistence.PersistenceManager;
import io.github.lucaseasedup.logit.persistence.PersistenceSerializer;
import io.github.lucaseasedup.logit.profile.ProfileManager;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.session.SessionManager;
import io.github.lucaseasedup.logit.storage.CacheType;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.storage.Storage.DataType;
import io.github.lucaseasedup.logit.storage.StorageFactory;
import io.github.lucaseasedup.logit.storage.StorageType;
import io.github.lucaseasedup.logit.storage.WrapperStorage;
import io.github.lucaseasedup.logit.tabapi.TabAPI;
import io.github.lucaseasedup.logit.util.IoUtils;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * The central part of LogIt.
 */
public final class LogItCore
{
    private LogItCore(LogItPlugin plugin)
    {
        assert plugin != null;
        
        this.plugin = plugin;
    }
    
    /**
     * Starts up the LogIt core.
     * 
     * @throws FatalReportedException if a critical error occurred
     *                                and LogIt could not start.
     *                                
     * @throws IllegalStateException  if the LogIt core is already running.
     * 
     * @see #isStarted()
     * @see #stop()
     */
    public void start() throws FatalReportedException
    {
        if (isStarted())
            throw new IllegalStateException("The LogIt core has already been started.");
        
        getDataFolder().mkdir();
        
        firstRun = !getDataFile("config.yml").exists();
        
        setUpConfiguration();
        setUpLogger();
        
        if (isFirstRun())
        {
            doFirstRunStuff();
        }
        
        if (getConfig("config.yml").getBoolean("checkForUpdates"))
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    checkForUpdate();
                }
            }.runTaskLater(getPlugin(), 1L);
        }
        
        setUpCraftReflect();
        setUpLocaleManager();
        setUpAccountManager();
        setUpPersistenceManager();
        
        securityHelper = new SecurityHelper();
        backupManager = new BackupManager(getAccountManager());
        sessionManager = new SessionManager();
        messageDispatcher = new LogItMessageDispatcher();
        tabCompleter = new LogItTabCompleter();
        
        if (getConfig("config.yml").getBoolean("profiles.enabled"))
        {
            setUpProfileManager();
        }
        
        globalPasswordManager = new GlobalPasswordManager();
        cooldownManager = new CooldownManager();
        accountWatcher = new AccountWatcher();
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null)
                {
                    tabApi = new TabAPI();
                    tabApi.onEnable();
                }
            }
        }.runTaskLater(getPlugin(), 1L);
        
        tabListUpdater = new TabListUpdater();
        
        startTasks();
        enableCommands();
        registerEvents();
        
        started = true;
        
        log(Level.FINE, _("startPlugin.success"));
        
        if (isFirstRun())
        {
            log(Level.INFO, _("firstRun"));
        }
    }
    
    private void setUpConfiguration() throws FatalReportedException
    {
        String configHeader = "# # # # # # # # # # # # # # #\n"
                            + " LogIt Configuration File   #\n"
                            + "# # # # # # # # # # # # # # #\n";
        
        String statsHeader = "# # # # # # # # # # # # # # #\n"
                           + "  LogIt Statistics File     #\n"
                           + "# # # # # # # # # # # # # # #\n";
        
        String secretHeader = "# # # # # # # # # # # # # # # # # # # # # # # # # # # #\n"
                            + "             LogIt Secret Settings File               #\n"
                            + "                                                      #\n"
                            + " Do not touch unless you are 100% what you're doing!  #\n"
                            + "# # # # # # # # # # # # # # # # # # # # # # # # # # # #\n";
        
        File oldConfigDefFile = getDataFile("config-def.b64");
        
        if (oldConfigDefFile.exists())
        {
            File newConfigDefFile = getDataFile(".doNotTouch/config-def.b64");
            
            try
            {
                newConfigDefFile.getParentFile().mkdirs();
                newConfigDefFile.createNewFile();
                
                Files.copy(oldConfigDefFile, newConfigDefFile);
                
                oldConfigDefFile.delete();
            }
            catch (IOException ex)
            {
                log(Level.WARNING, ex);
            }
        }
        
        configurationManager = new ConfigurationManager();
        configurationManager.registerConfiguration("config.yml",
                ".doNotTouch/config-def.b64", "config-def.ini", configHeader);
        configurationManager.registerConfiguration("stats.yml",
                ".doNotTouch/stats-def.b64", "stats-def.ini", statsHeader);
        configurationManager.registerConfiguration("secret.yml",
                ".doNotTouch/secret-def.b64", "secret-def.ini", secretHeader);
        
        try
        {
            configurationManager.loadAll();
        }
        catch (IOException | InvalidConfigurationException ex)
        {
            log(Level.SEVERE, "Could not load a configuration file.", ex);
            
            FatalReportedException.throwNew(ex);
        }
        catch (InvalidPropertyValueException ex)
        {
            log(Level.SEVERE, ex.getMessage());
            
            FatalReportedException.throwNew(ex);
        }
    }
    
    private void setUpLogger()
    {
        logger = new LogItCoreLogger(this);
        logger.open();
    }
    
    private void doFirstRunStuff()
    {
        getDataFile("backup").mkdir();
        getDataFile("mail").mkdir();
        getDataFile("lang").mkdir();
        
        File passwordRecoveryTemplateFile = getDataFile("mail/password-recovery.html");
        
        if (!passwordRecoveryTemplateFile.exists())
        {
            try
            {
                IoUtils.extractResource("password-recovery.html",
                        passwordRecoveryTemplateFile);
            }
            catch (IOException ex)
            {
                log(Level.WARNING, "Could not copy resource: password-recovery.html", ex);
            }
        }
    }
    
    private void checkForUpdate()
    {
        try
        {
            String updateVersion = UpdateChecker.checkForUpdate(
                    getPlugin().getDescription().getFullName()
            );
            
            if (updateVersion != null)
            {
                log(Level.INFO, _("updateAvailable")
                        .replace("{0}", String.valueOf(updateVersion))
                        .replace("{1}", "http://dev.bukkit.org/bukkit-plugins/logit/"));
            }
        }
        catch (IOException ex)
        {
            // If a connection to the remote host could not be established,
            // we can't do anything about it and neither the user can,
            // so we'll just act as it never happened.
        }
        catch (ParseException ex)
        {
            log(Level.WARNING, ex);
        }
    }
    
    private void setUpCraftReflect()
    {
        try
        {
            String version = LogItPlugin.getCraftBukkitVersion();
            String craftClassName =
                    "io.github.lucaseasedup.logit.craftreflect." + version + ".CraftReflect";
            Class<?> craftClass = Class.forName(craftClassName);
            
            craftReflect = (CraftReflect) craftClass.getConstructor().newInstance();
        }
        catch (ClassNotFoundException ex)
        {
            log(Level.WARNING, "LogIt does not support this version of Bukkit."
                    + " Some features may not work.");
        }
        catch (ReflectiveOperationException ex)
        {
            log(Level.WARNING, "Could not set up CraftBukkit reflection."
                    + " Some features may not work.", ex);
        }
    }
    
    private void setUpLocaleManager()
    {
        localeManager = new LocaleManager();
        localeManager.registerLocale(EnglishLocale.getInstance());
        localeManager.registerLocale(PolishLocale.getInstance());
        localeManager.registerLocale(GermanLocale.getInstance());
        localeManager.setFallbackLocale(EnglishLocale.class);
        localeManager.switchActiveLocale(getConfig("config.yml").getString("locale"));
    }
    
    private void setUpAccountManager() throws FatalReportedException
    {
        StorageType leadingStorageType = StorageType.decode(
                getConfig("config.yml").getString("storage.accounts.leading.storageType")
        );
        StorageType mirrorStorageType = StorageType.decode(
                getConfig("config.yml").getString("storage.accounts.mirror.storageType")
        );
        
        String accountsUnit = getConfig("config.yml").getString("storage.accounts.leading.unit");
        AccountKeys accountKeys = new AccountKeys(
            getConfig("config.yml").getString("storage.accounts.keys.username"),
            getConfig("config.yml").getString("storage.accounts.keys.uuid"),
            getConfig("config.yml").getString("storage.accounts.keys.salt"),
            getConfig("config.yml").getString("storage.accounts.keys.password"),
            getConfig("config.yml").getString("storage.accounts.keys.hashing_algorithm"),
            getConfig("config.yml").getString("storage.accounts.keys.ip"),
            getConfig("config.yml").getString("storage.accounts.keys.login_session"),
            getConfig("config.yml").getString("storage.accounts.keys.email"),
            getConfig("config.yml").getString("storage.accounts.keys.last_active_date"),
            getConfig("config.yml").getString("storage.accounts.keys.reg_date"),
            getConfig("config.yml").getString("storage.accounts.keys.is_locked"),
            getConfig("config.yml").getString("storage.accounts.keys.login_history"),
            getConfig("config.yml").getString("storage.accounts.keys.display_name"),
            getConfig("config.yml").getString("storage.accounts.keys.persistence")
        );
        
        Storage leadingAccountStorage =
                new StorageFactory(getConfig("config.yml"), "storage.accounts.leading")
                        .produceStorage(leadingStorageType);
        
        Storage mirrorAccountStorage = 
                new StorageFactory(getConfig("config.yml"), "storage.accounts.mirror")
                        .produceStorage(mirrorStorageType);
        
        CacheType accountCacheType = CacheType.decode(
                getConfig("config.yml").getString("storage.accounts.leading.cache")
        );
        
        @SuppressWarnings("resource")
        WrapperStorage accountStorage = new WrapperStorage.Builder()
                .leading(leadingAccountStorage)
                .cacheType(accountCacheType)
                .build();
        accountStorage.mirrorStorage(mirrorAccountStorage,
                new Hashtable<String, String>()
                {
                    private static final long serialVersionUID = 1L;
                    
                    {
                        put(getConfig("config.yml").getString("storage.accounts.leading.unit"),
                            getConfig("config.yml").getString("storage.accounts.mirror.unit"));
                    }
                }
        );
        
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
            accountStorage.createUnit(accountsUnit, accountKeys, accountKeys.username());
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
                accountStorage.selectEntries(
                        getConfig("config.yml").getString("storage.accounts.leading.unit")
                );
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, "Could not preload accounts.", ex);
            }
        }
        
        accountManager = new AccountManager(accountStorage, accountsUnit, accountKeys);
    }
    
    private void setUpPersistenceManager() throws FatalReportedException
    {
        persistenceManager = new PersistenceManager();
        
        setSerializerEnabled(LocationSerializer.class,
                getConfig("config.yml").getBoolean("waitingRoom.enabled"));
        setSerializerEnabled(AirBarSerializer.class,
                getConfig("config.yml").getBoolean("forceLogin.obfuscate.air"));
        setSerializerEnabled(HealthBarSerializer.class,
                getConfig("config.yml").getBoolean("forceLogin.obfuscate.health"));
        setSerializerEnabled(ExperienceSerializer.class,
                getConfig("config.yml").getBoolean("forceLogin.obfuscate.experience"));
        setSerializerEnabled(HungerBarSerializer.class,
                getConfig("config.yml").getBoolean("forceLogin.obfuscate.hunger"));
    }
    
    private void setSerializerEnabled(Class<? extends PersistenceSerializer> clazz, boolean status)
            throws FatalReportedException
    {
        if (status)
        {
            try
            {
                getPersistenceManager().registerSerializer(clazz);
            }
            catch (ReflectiveOperationException ex)
            {
                log(Level.SEVERE,
                        "Could not register persistence serializer: " + clazz.getSimpleName());
                
                FatalReportedException.throwNew(ex);
            }
        }
        else
        {
            AccountKeys keys = getAccountManager().getKeys();
            
            for (Player player : Bukkit.getOnlinePlayers())
            {
                Account account = getAccountManager().selectAccount(player.getName(),
                        Arrays.asList(
                                keys.username(),
                                keys.persistence()
                        )
                );
                
                if (account != null)
                {
                    getPersistenceManager().unserializeUsing(account, player, clazz);
                }
            }
        }
    }
    
    private void setUpProfileManager()
    {
        File profilesPath = getDataFile(getConfig("config.yml").getString("profiles.path"));
        
        if (!profilesPath.exists())
        {
            profilesPath.getParentFile().mkdirs();
            profilesPath.mkdir();
        }
        
        profileManager = new ProfileManager(profilesPath,
                getConfig("config.yml").getValues("profiles.fields"));
    }
    
    private void startTasks()
    {
        accountManagerTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),
                getAccountManager(), 0L,
                getConfig("secret.yml").getTime("bufferFlushInterval", TimeUnit.TICKS));
        backupManagerTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),
                getBackupManager(), 0L,
                BackupManager.TASK_PERIOD);
        sessionManagerTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),
                getSessionManager(), 0L,
                SessionManager.TASK_PERIOD);
        globalPasswordManagerTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),
                getGlobalPasswordManager(), 0L,
                GlobalPasswordManager.TASK_PERIOD);
        accountWatcherTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),
                getAccountWatcher(), 0L,
                AccountWatcher.TASK_PERIOD);
        tabListUpdaterTask = Bukkit.getScheduler().runTaskTimer(getPlugin(),
                tabListUpdater, 20L,
                TabListUpdater.TASK_PERIOD);
    }
    
    private void enableCommands()
    {
        enableCommand("login", new LoginCommand());
        enableCommand("logout", new LogoutCommand());
        enableCommand("remember", new RememberCommand(),
                getConfig("config.yml").getBoolean("loginSessions.enabled"));
        enableCommand("register", new RegisterCommand());
        enableCommand("unregister", new UnregisterCommand());
        enableCommand("changepass", new ChangePassCommand(),
                !getConfig("config.yml").getBoolean("passwords.disable"));
        enableCommand("changeemail", new ChangeEmailCommand());
        enableCommand("recoverpass", new RecoverPassCommand(),
                getConfig("config.yml").getBoolean("passwordRecovery.enabled"));
        enableCommand("profile", new ProfileCommand(),
                getConfig("config.yml").getBoolean("profiles.enabled"));
        enableCommand("acclock", new AcclockCommand());
        enableCommand("accunlock", new AccunlockCommand());
        enableCommand("loginhistory", new LoginHistoryCommand(),
                getConfig("config.yml").getBoolean("loginHistory.enabled"));
        enableCommand("$logit-nop-command", new NopCommandExecutor());
    }
    
    private void enableCommand(String command, CommandExecutor executor, boolean enabled)
    {
        if (enabled)
        {
            getPlugin().getCommand(command).setExecutor(executor);
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
        PlayerCollections.registerListener(getPlugin());
        
        Bukkit.getPluginManager().registerEvents(getMessageDispatcher(), getPlugin());
        Bukkit.getPluginManager().registerEvents(getCooldownManager(), getPlugin());
        Bukkit.getPluginManager().registerEvents(getTabListUpdater(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new ServerEventListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new EntityEventListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new InventoryEventListener(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new SessionEventListener(), getPlugin());
    }
    
    /**
     * Stops the LogIt core.
     * 
     * @throws IllegalStateException if the LogIt core is not running.
     * 
     * @see #isStarted()
     * @see #start()
     */
    public void stop()
    {
        if (!isStarted())
            throw new IllegalStateException("The LogIt core is not started.");
        
        disableCommands();
        
        getPersistenceManager().unregisterSerializer(LocationSerializer.class);
        getPersistenceManager().unregisterSerializer(AirBarSerializer.class);
        getPersistenceManager().unregisterSerializer(HealthBarSerializer.class);
        getPersistenceManager().unregisterSerializer(ExperienceSerializer.class);
        getPersistenceManager().unregisterSerializer(HungerBarSerializer.class);
        
        try
        {
            getAccountManager().getStorage().close();
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not close database connection.", ex);
        }
        
        Bukkit.getScheduler().cancelTasks(getPlugin());
        
        // Unregister all event listeners.
        HandlerList.unregisterAll(getPlugin());
        
        started = false;
        
        dispose();
        
        log(Level.FINE, _("stopPlugin.success"));
        
        if (logger != null)
        {
            logger.close();
            logger = null;
        }
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
        disableCommand("loginhistory");
        disableCommand("$logit-nop-command");
    }
    
    private void disableCommand(String command)
    {
        getPlugin().getCommand(command).setExecutor(new DisabledCommandExecutor());
    }
    
    /**
     * Disposes the LogIt core.
     * 
     * @throws IllegalStateException if the LogIt core is running.
     */
    private void dispose()
    {
        if (isStarted())
            throw new IllegalStateException("Cannot dispose the LogIt core while it's running.");
        
        if (configurationManager != null)
        {
            configurationManager.dispose();
            configurationManager = null;
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
        
        securityHelper = null;
        
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
        
        if (messageDispatcher != null)
        {
            messageDispatcher.dispose();
            messageDispatcher = null;
        }
        
        if (tabCompleter != null)
        {
            tabCompleter.dispose();
            tabCompleter = null;
        }
        
        if (profileManager != null)
        {
            profileManager.dispose();
            profileManager = null;
        }
        
        if (globalPasswordManager != null)
        {
            globalPasswordManager.dispose();
            globalPasswordManager = null;
        }
        
        if (cooldownManager != null)
        {
            cooldownManager.dispose();
            cooldownManager = null;
        }
        
        if (accountWatcher != null)
        {
            accountWatcher.dispose();
            accountWatcher = null;
        }
        
        if (tabApi != null)
        {
            tabApi.onDisable();
            tabApi = null;
        }
        
        tabListUpdater = null;
    }
    
    /**
     * Restarts the LogIt core.
     * 
     * @throws FatalReportedException if the LogIt core could not be started again.
     * @throws IllegalStateException  if the LogIt core is not running.
     * 
     * @see #isStarted()
     * @see #start()
     */
    public void restart() throws FatalReportedException
    {
        if (!isStarted())
            throw new IllegalStateException("The LogIt core is not started.");
        
        File sessionsFile =
                getDataFile(getConfig("config.yml").getString("storage.sessions.filename"));
        
        try
        {
            sessionManager.exportSessions(sessionsFile);
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not export sessions.", ex);
        }
        
        stop();
        start();
        
        try
        {
            getPlugin().reloadMessages(getConfig("config.yml").getString("locale"));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, "Could not load messages.", ex);
        }
        
        if (sessionsFile.exists())
        {
            try
            {
                sessionManager.importSessions(sessionsFile);
            }
            catch (IOException ex)
            {
                log(Level.WARNING, "Could not import sessions.", ex);
            }
            
            sessionsFile.delete();
        }
        
        log(Level.INFO, _("reloadPlugin.success"));
    }
    
    /**
     * Checks if a player is forced to log in.
     * 
     * <p> Returns {@code true} if the <i>forceLogin.global</i> config setting
     * is set to <i>true</i>, or the player is in a world with forced login.
     * 
     * <p> If the player name is contained in the <i>forceLogin.exemptPlayers</i>
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
        String playerWorldName = player.getWorld().getName();
        
        boolean forcedLoginGlobal =
                getConfig("config.yml").getBoolean("forceLogin.global");
        List<String> exemptedWorlds =
                getConfig("config.yml").getStringList("forceLogin.inWorlds");
        List<String> exemptedPlayers =
                getConfig("config.yml").getStringList("forceLogin.exemptPlayers");
        
        return (forcedLoginGlobal || exemptedWorlds.contains(playerWorldName))
                && !containsIgnoreCase(player.getName(), exemptedPlayers);
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
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public void updatePlayerGroup(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (!VaultHook.isVaultEnabled())
            return;
        
        if (getAccountManager().isRegistered(player.getName()))
        {
            VaultHook.playerRemoveGroup(player,
                    getConfig("config.yml").getString("groups.unregistered"));
            VaultHook.playerAddGroup(player,
                    getConfig("config.yml").getString("groups.registered"));
        }
        else
        {
            VaultHook.playerRemoveGroup(player,
                    getConfig("config.yml").getString("groups.registered"));
            VaultHook.playerAddGroup(player,
                    getConfig("config.yml").getString("groups.unregistered"));
        }
        
        if (getSessionManager().isSessionAlive(player))
        {
            VaultHook.playerRemoveGroup(player,
                    getConfig("config.yml").getString("groups.loggedOut"));
            VaultHook.playerAddGroup(player,
                    getConfig("config.yml").getString("groups.loggedIn"));
        }
        else
        {
            VaultHook.playerRemoveGroup(player,
                    getConfig("config.yml").getString("groups.loggedIn"));
            VaultHook.playerAddGroup(player,
                    getConfig("config.yml").getString("groups.loggedOut"));
        }
    }
    
    public void log(Level level, String msg)
    {
        if (getLogger() == null)
        {
            getPlugin().getLogger().log(level, ChatColor.stripColor(msg));
        }
        else
        {
            getLogger().log(level, msg);
        }
    }
    
    public void log(Level level, String msg, Throwable throwable)
    {
        if (getLogger() == null)
        {
            getPlugin().getLogger().log(level, ChatColor.stripColor(msg), throwable);
        }
        else
        {
            getLogger().log(level, msg, throwable);
        }
    }
    
    public void log(Level level, Throwable throwable)
    {
        if (getLogger() == null)
        {
            getPlugin().getLogger().log(level, null, throwable);
        }
        else
        {
            getLogger().log(level, throwable);
        }
    }
    
    /**
     * Returns the LogIt plugin object.
     * 
     * <p> Most of times, all the work will be done with the LogIt core,
     * but the {@code LogItPlugin} may come useful if you want to,
     * for example, reload the message files.
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
        return getPlugin().getDataFolder();
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
    
    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }
    
    public PredefinedConfiguration getConfig(String filename)
    {
        if (filename == null)
            throw new IllegalArgumentException();
        
        if (getConfigurationManager() == null)
            return null;
        
        return getConfigurationManager().getConfiguration(filename);
    }
    
    public Location getWaitingRoomLocation()
    {
        return getConfig("config.yml").getLocation("waitingRoom.location").toBukkitLocation();
    }
    
    public IntegrationType getIntegrationType()
    {
        return IntegrationType.decode(
                getConfig("config.yml").getString("integration")
        );
    }
    
    private LogItCoreLogger getLogger()
    {
        return logger;
    }
    
    protected CraftReflect getCraftReflect()
    {
        return craftReflect;
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
    
    public SecurityHelper getSecurityHelper()
    {
        return securityHelper;
    }
    
    public BackupManager getBackupManager()
    {
        return backupManager;
    }
    
    public SessionManager getSessionManager()
    {
        return sessionManager;
    }
    
    public LogItMessageDispatcher getMessageDispatcher()
    {
        return messageDispatcher;
    }
    
    public LogItTabCompleter getTabCompleter()
    {
        return tabCompleter;
    }
    
    public ProfileManager getProfileManager()
    {
        return profileManager;
    }
    
    public GlobalPasswordManager getGlobalPasswordManager()
    {
        return globalPasswordManager;
    }
    
    public CooldownManager getCooldownManager()
    {
        return cooldownManager;
    }
    
    private AccountWatcher getAccountWatcher()
    {
        return accountWatcher;
    }
    
    /* package */ TabAPI getTabApi()
    {
        return tabApi;
    }
    
    public TabListUpdater getTabListUpdater()
    {
        return tabListUpdater;
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
    
    private static volatile LogItCore instance = null;
    
    private final LogItPlugin plugin;
    private boolean firstRun;
    private boolean started = false;
    
    private ConfigurationManager configurationManager;
    private LogItCoreLogger logger;
    private CraftReflect craftReflect;
    private LocaleManager localeManager;
    private AccountManager accountManager;
    private PersistenceManager persistenceManager;
    private SecurityHelper securityHelper;
    private BackupManager backupManager;
    private SessionManager sessionManager;
    private LogItMessageDispatcher messageDispatcher;
    private LogItTabCompleter tabCompleter;
    private ProfileManager profileManager;
    private GlobalPasswordManager globalPasswordManager;
    private CooldownManager cooldownManager;
    private AccountWatcher accountWatcher;
    private TabAPI tabApi;
    private TabListUpdater tabListUpdater;
    
    private BukkitTask tabListUpdaterTask;
    private BukkitTask accountManagerTask;
    private BukkitTask backupManagerTask;
    private BukkitTask sessionManagerTask;
    private BukkitTask globalPasswordManagerTask;
    private BukkitTask accountWatcherTask;
}
