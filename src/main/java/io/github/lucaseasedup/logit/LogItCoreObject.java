package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.backup.BackupManager;
import io.github.lucaseasedup.logit.command.LogItTabCompleter;
import io.github.lucaseasedup.logit.common.Disposable;
import io.github.lucaseasedup.logit.config.ConfigurationManager;
import io.github.lucaseasedup.logit.config.PredefinedConfiguration;
import io.github.lucaseasedup.logit.cooldown.CooldownManager;
import io.github.lucaseasedup.logit.locale.LocaleManager;
import io.github.lucaseasedup.logit.message.LogItMessageDispatcher;
import io.github.lucaseasedup.logit.persistence.PersistenceManager;
import io.github.lucaseasedup.logit.profile.ProfileManager;
import io.github.lucaseasedup.logit.security.GlobalPasswordManager;
import io.github.lucaseasedup.logit.security.SecurityHelper;
import io.github.lucaseasedup.logit.session.SessionManager;
import java.io.File;
import java.util.logging.Level;

/**
 * Provides a convenient way for objects to interact with the LogIt core.
 */
public abstract class LogItCoreObject implements Disposable
{
    /**
     * Constructs a new {@code LogItCoreObject}.
     * 
     * @throws IllegalStateException if no {@code LogItCore} instance could be found.
     */
    public LogItCoreObject()
    {
        core = LogItCore.getInstance();
        
        if (core == null)
        {
            throw new IllegalStateException("No LogItCore instance found.");
        }
    }
    
    @Override
    public void dispose()
    {
        // Left for optional implementation by extending classes.
    }
    
    protected final LogItCore getCore()
    {
        return core;
    }
    
    protected final void log(Level level, String msg)
    {
        getCore().log(level, msg);
    }
    
    protected final void log(Level level, String msg, Throwable throwable)
    {
        getCore().log(level, msg, throwable);
    }
    
    protected final void log(Level level, Throwable throwable)
    {
        getCore().log(level, throwable);
    }
    
    protected final LogItPlugin getPlugin()
    {
        return getCore().getPlugin();
    }
    
    protected final boolean isCoreStarted()
    {
        return getCore().isStarted();
    }
    
    protected final File getDataFolder()
    {
        return getCore().getPlugin().getDataFolder();
    }
    
    protected final File getDataFile(String path)
    {
        return getCore().getDataFile(path);
    }
    
    protected final File getDataFile(String parent, String path)
    {
        return getCore().getDataFile(parent, path);
    }
    
    protected final ConfigurationManager getConfigurationManager()
    {
        return getCore().getConfigurationManager();
    }
    
    protected final PredefinedConfiguration getConfig(String filename)
    {
        return getCore().getConfig(filename);
    }
    
    protected final LocaleManager getLocaleManager()
    {
        return getCore().getLocaleManager();
    }
    
    protected final AccountManager getAccountManager()
    {
        return getCore().getAccountManager();
    }
    
    protected final AccountKeys keys()
    {
        return getCore().getAccountManager().getKeys();
    }
    
    protected final PersistenceManager getPersistenceManager()
    {
        return getCore().getPersistenceManager();
    }
    
    protected final SecurityHelper getSecurityHelper()
    {
        return getCore().getSecurityHelper();
    }
    
    protected final BackupManager getBackupManager()
    {
        return getCore().getBackupManager();
    }
    
    protected final SessionManager getSessionManager()
    {
        return getCore().getSessionManager();
    }
    
    protected final LogItMessageDispatcher getMessageDispatcher()
    {
        return getCore().getMessageDispatcher();
    }
    
    protected final LogItTabCompleter getTabCompleter()
    {
        return getCore().getTabCompleter();
    }
    
    protected final ProfileManager getProfileManager()
    {
        return getCore().getProfileManager();
    }
    
    protected final GlobalPasswordManager getGlobalPasswordManager()
    {
        return getCore().getGlobalPasswordManager();
    }
    
    protected final CooldownManager getCooldownManager()
    {
        return getCore().getCooldownManager();
    }
    
    private final LogItCore core;
}
