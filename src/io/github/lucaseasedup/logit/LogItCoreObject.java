package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.config.LogItConfiguration;
import io.github.lucaseasedup.logit.inventory.InventoryDepository;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.session.SessionManager;
import java.io.File;
import java.util.logging.Level;

public abstract class LogItCoreObject
{
    public LogItCoreObject(LogItCore core)
    {
        this.core = core;
    }
    
    public LogItCore getCore()
    {
        return core;
    }
    
    public LogItPlugin getPlugin()
    {
        return core.getPlugin();
    }
    
    public File getDataFolder()
    {
        return core.getPlugin().getDataFolder();
    }
    
    public LogItConfiguration getConfig()
    {
        if (!core.isConfigLoaded())
            return null;
        
        return core.getConfig();
    }
    
    public AccountManager getAccountManager()
    {
        return core.getAccountManager();
    }
    
    public BackupManager getBackupManager()
    {
        return core.getBackupManager();
    }
    
    public SessionManager getSessionManager()
    {
        return core.getSessionManager();
    }
    
    public WaitingRoom getWaitingRoom()
    {
        return core.getWaitingRoom();
    }
    
    public InventoryDepository getInventoryDepository()
    {
        return core.getInventoryDepository();
    }
    
    public MailSender getMailSender()
    {
        return core.getMailSender();
    }
    
    public void log(Level level, String msg)
    {
        if (core.isConfigLoaded())
        {
            core.log(level, msg);
        }
        else
        {
            core.getPlugin().getLogger().log(level, msg);
        }
    }
    
    public void log(Level level, String msg, Throwable t)
    {
        if (core.isConfigLoaded())
        {
            core.log(level, msg, t);
        }
        else
        {
            core.getPlugin().getLogger().log(level, msg, t);
        }
    }
    
    private final LogItCore core;
}
