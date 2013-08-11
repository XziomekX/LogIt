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

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.Collections;
import java.util.Set;

/**
 * @author LucasEasedUp
 */
public class AccountWatcher extends LogItCoreObject implements Runnable
{
    public AccountWatcher(LogItCore core)
    {
        super(core);
    }
    
    @Override
    public void run()
    {
        if (getConfig().getInt("crowd-control.days-of-absence-to-unregister") < 0)
            return;
        
        Set<String> usernames = Collections.synchronizedSet(getAccountManager().getRegisteredUsernames());
        int now = (int) (System.currentTimeMillis() / 1000L);
        
        for (String username : usernames)
        {
            int lastActiveDate = getAccountManager().getLastActiveDate(username);
            
            if (lastActiveDate == 0)
                continue;
            
            int absenceTime = (now - lastActiveDate);
            
            if (absenceTime >= (getConfig().getInt("crowd-control.days-of-absence-to-unregister") * 86400))
            {
                getAccountManager().removeAccount(username);
            }
        }
    }
}
