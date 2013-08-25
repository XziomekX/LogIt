/*
 * AccountWatcher.java
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
package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.Collections;
import java.util.Set;

/**
 * @author LucasEasedUp
 */
public final class AccountWatcher extends LogItCoreObject implements Runnable
{
    @Override
    public void run()
    {
        int daysOfAbsenceToUnregister =
                getConfig().getInt("crowd-control.days-of-absence-to-unregister");
        
        if (daysOfAbsenceToUnregister < 0)
            return;
        
        Set<String> usernames =
                Collections.synchronizedSet(getAccountManager().getRegisteredUsernames());
        long now = System.currentTimeMillis() / 1000L;
        
        for (String username : usernames)
        {
            long lastActiveDate =
                    getAccountManager().getAccount(username).getLong("logit.accounts.last_active");
            
            if (lastActiveDate == 0)
                continue;
            
            long absenceTime = (now - lastActiveDate);
            
            if (absenceTime >= (daysOfAbsenceToUnregister * 86400))
            {
                getAccountManager().removeAccount(username);
            }
        }
    }
}
