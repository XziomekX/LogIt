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
import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorBinary;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SelectorNegation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AccountWatcher extends LogItCoreObject implements Runnable
{
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void run()
    {
        boolean accountDeletionEnabled =
                getConfig("config.yml").getBoolean("automaticAccountDeletion.enabled");
        
        if (!accountDeletionEnabled)
            return;
        
        long now = System.currentTimeMillis() / 1000L;
        long inactivityTime =
                getConfig("config.yml").getTime("automaticAccountDeletion.inactivityTime",
                        TimeUnit.SECONDS);
        
        List<Account> accounts = getAccountManager().selectAccounts(
                Arrays.asList(keys().username(), keys().last_active_date()),
                new SelectorBinary(
                    new SelectorCondition(
                        keys().last_active_date(),
                        Infix.GREATER_THAN,
                        "0"
                    ),
                    Infix.AND,
                    new SelectorNegation(
                        new SelectorCondition(
                            keys().last_active_date(),
                            Infix.GREATER_THAN,
                            String.valueOf(now - inactivityTime)
                        )
                    )
                )
        );
        
        if (!accounts.isEmpty())
        {
            List<String> accountsToDelete = new ArrayList<>();
            
            for (Account account : accounts)
            {
                String username = account.getUsername();
                
                if (!getSessionManager().isSessionAlive(username))
                {
                    accountsToDelete.add(username);
                }
            }
            
            getAccountManager().removeAccounts(
                    accountsToDelete.toArray(new String[accountsToDelete.size()])
            );
        }
    }
    
    /**
     * Recommended task period of {@code AccountWatcher} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = TimeUnit.MINUTES.convert(10, TimeUnit.TICKS);
}
