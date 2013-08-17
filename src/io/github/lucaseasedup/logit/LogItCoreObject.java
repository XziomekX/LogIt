/*
 * LogItCoreObject.java
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

import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.config.LogItConfiguration;
import io.github.lucaseasedup.logit.inventory.InventoryDepository;
import io.github.lucaseasedup.logit.mail.MailSender;
import io.github.lucaseasedup.logit.session.SessionManager;
import java.io.File;
import java.util.logging.Level;

/**
 * @author LucasEasedUp
 */
public abstract class LogItCoreObject
{
    public LogItCoreObject(LogItCore core)
    {
        this.core = core;
    }
    
    protected LogItCore getCore()
    {
        return core;
    }
    
    protected LogItPlugin getPlugin()
    {
        return core.getPlugin();
    }
    
    protected File getDataFolder()
    {
        return core.getPlugin().getDataFolder();
    }
    
    protected LogItConfiguration getConfig()
    {
        if (!core.isConfigLoaded())
            return null;
        
        return core.getConfig();
    }
    
    protected AccountManager getAccountManager()
    {
        return core.getAccountManager();
    }
    
    protected BackupManager getBackupManager()
    {
        return core.getBackupManager();
    }
    
    protected SessionManager getSessionManager()
    {
        return core.getSessionManager();
    }
    
    protected WaitingRoom getWaitingRoom()
    {
        return core.getWaitingRoom();
    }
    
    protected InventoryDepository getInventoryDepository()
    {
        return core.getInventoryDepository();
    }
    
    protected MailSender getMailSender()
    {
        return core.getMailSender();
    }
    
    protected void log(Level level, String msg)
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
    
    protected void log(Level level, String msg, Throwable throwable)
    {
        if (core.isConfigLoaded())
        {
            core.log(level, msg, throwable);
        }
        else
        {
            core.getPlugin().getLogger().log(level, msg, throwable);
        }
    }
    
    private final LogItCore core;
}
