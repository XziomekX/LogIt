/*
 * LogItTabCompleter.java
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
package io.github.lucaseasedup.logit.command;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LogItTabCompleter extends LogItCoreObject
{
    public List<String> completeUsername(String username)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        List<Account> accounts = getAccountManager().selectAccounts(
                Arrays.asList(keys().username()),
                new SelectorCondition(
                        keys().username(),
                        Infix.STARTS_WITH,
                        username
                )
        );
        
        if (accounts == null)
            return null;
        
        List<String> suggestions = new ArrayList<>();
        
        for (Account account : accounts)
        {
            suggestions.add(account.getUsername());
        }
        
        Collections.sort(suggestions);
        
        return suggestions.subList(0,
                (suggestions.size() < MAX_SUGGESTIONS)
                        ? suggestions.size() : MAX_SUGGESTIONS);
    }
    
    private static final int MAX_SUGGESTIONS = 8;
}
