/*
 * GlobalPasswordManager.java
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
package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.security.SecurityHelper;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class GlobalPasswordManager extends LogItCoreObject implements Runnable, Disposable
{
    @Override
    public void dispose()
    {
        if (passwords != null)
        {
            passwords.clear();
            passwords = null;
        }
    }
    
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void run()
    {
        for (Iterator<Entry<String, Long>> it = passwords.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, Long> e = it.next();
            
            if (e.getValue() <= 0)
            {
                it.remove();
            }
            
            e.setValue(e.getValue() - TASK_PERIOD);
        }
    }
    
    public boolean checkPassword(String password)
    {
        for (Entry<String, Long> e : passwords.entrySet())
        {
            if (e.getKey().equals(password) && e.getValue() > 0)
            {
                passwords.remove(e.getKey());
                
                return true;
            }
        }
        
        return false;
    }
    
    public String generatePassword()
    {
        int length = getConfig().getInt("password.global-password.length");
        String password = SecurityHelper.generatePassword(length, "0123456789");
        long lifetimeTicks =
                getConfig().getTime("password.global-password.invalidate-after", TimeUnit.TICKS);
        
        passwords.put(password, lifetimeTicks);
        
        return password;
    }
    
    /**
     * Recommended task period of {@code GlobalPasswordManager} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = TimeUnit.SECONDS.convert(1, TimeUnit.TICKS);
    
    private Map<String, Long> passwords = new Hashtable<>();
}
