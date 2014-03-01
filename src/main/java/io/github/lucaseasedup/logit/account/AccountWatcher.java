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
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

public final class AccountWatcher extends LogItCoreObject implements Runnable
{
    @Override
    public void run()
    {
        boolean accountDeletionEnabled =
                getConfig().getBoolean("crowd-control.automatic-account-deletion.enabled");
        
        if (!accountDeletionEnabled)
            return;
        
        long inactivityTime =
                getConfig().getTime("crowd-control.automatic-account-deletion.inactivity-time",
                        TimeUnit.SECONDS);
        
        try
        {
            AccountKeys keys = getAccountManager().getKeys();
            List<Hashtable<String, String>> rs =
                    getAccountStorage().selectEntries(getAccountManager().getUnit(),
                            Arrays.asList(keys.username(), keys.last_active_date()));
            long now = System.currentTimeMillis() / 1000L;
            
            for (Hashtable<String, String> entry : rs)
            {
                if (getSessionManager().isSessionAlive(entry.get(keys.username())))
                    continue;
                
                String lastActiveDateString = entry.get(keys.last_active_date());
                
                if (lastActiveDateString.isEmpty())
                    continue;
                
                long lastActiveDate = Long.parseLong(lastActiveDateString);
                long absenceTime = now - lastActiveDate;
                
                if (absenceTime >= inactivityTime)
                {
                    getAccountManager().removeAccount(entry.get(keys.username()));
                }
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
        }
    }
    
    public static final long TASK_PERIOD = (15 * 60) * 20;
}
