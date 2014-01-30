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

import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import java.util.Arrays;
import io.github.lucaseasedup.logit.util.HashtableBuilder;
import java.util.Hashtable;
import java.util.List;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public final class AccountWatcher extends LogItCoreObject implements Runnable
{
    @Override
    public void run()
    {
        int daysOfAbsenceToUnregister =
                getConfig().getInt("crowd-control.days-of-absence-to-unregister");
        
        if (daysOfAbsenceToUnregister < 0)
            return;
        
        try
        {
            AccountKeys keys = getAccountManager().getKeys();
            List<Hashtable<String, String>> rs =
                    getAccountManager().getStorage().selectEntries(getAccountManager().getUnit(),
                            Arrays.asList(keys.username(), keys.last_active_date()));
            long now = System.currentTimeMillis() / 1000L;
            
            for (Hashtable<String, String> entry : rs)
            {
                String lastActiveDateString = entry.get(keys.last_active_date());
                
                if (lastActiveDateString.isEmpty())
                    continue;
                
                long lastActiveDate = Long.parseLong(lastActiveDateString);
                long absenceTime = (now - lastActiveDate);
                
                if (absenceTime >= (daysOfAbsenceToUnregister * 86400))
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
    
    public static final long TASK_PERIOD = (10 * 60) * 20;
}
