/*
 * AccountWatcher.java
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
package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SelectorNegation;
import io.github.lucaseasedup.logit.storage.Storage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public final class AccountWatcher extends LogItCoreObject implements Runnable
{
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void run()
    {
        boolean accountDeletionEnabled =
                getConfig().getBoolean("crowd-control.automatic-account-deletion.enabled");
        
        if (!accountDeletionEnabled)
            return;
        
        long now = System.currentTimeMillis() / 1000L;
        long inactivityTime =
                getConfig().getTime("crowd-control.automatic-account-deletion.inactivity-time",
                        TimeUnit.SECONDS);
        
        try
        {
            AccountKeys keys = getAccountManager().getKeys();
            List<Storage.Entry> entries =
                    getAccountStorage().selectEntries(getAccountManager().getUnit(),
                            Arrays.asList(keys.username(), keys.last_active_date()),
                            new SelectorNegation(
                                new SelectorCondition(
                                    keys.last_active_date(),
                                    Infix.GREATER_THAN,
                                    String.valueOf(now - inactivityTime)
                                )
                            ));
            
            if (!entries.isEmpty())
            {
                getAccountStorage().setAutobatchEnabled(true);
                
                for (Storage.Entry entry : entries)
                {
                    String username = entry.get(keys.username());
                    
                    if (!getSessionManager().isSessionAlive(username))
                    {
                        getAccountManager().removeAccount(username);
                    }
                }
                
                getAccountStorage().executeBatch();
                getAccountStorage().clearBatch();
                getAccountStorage().setAutobatchEnabled(false);
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
        }
    }
    
    /**
     * Recommended task period of {@code AccountWatcher} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = (10 * 60) * 20;
}
