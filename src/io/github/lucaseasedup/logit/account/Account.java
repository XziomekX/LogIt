/*
 * Account.java
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

import io.github.lucaseasedup.logit.db.SetClause;
import io.github.lucaseasedup.logit.db.Table;
import io.github.lucaseasedup.logit.db.WhereClause;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.bukkit.Bukkit;

/**
 * @author LucasEasedUp
 */
public class Account
{
    public Account(Table table, Map<String, String> initialData)
    {
        this.data = new HashMap<>(initialData);
        this.table = table;
    }
    
    public String get(String property)
    {
        return data.get(property);
    }
    
    public Map<String, String> getAll()
    {
        return new HashMap<>(data);
    }
    
    public boolean update(String property, String value) throws SQLException
    {
        AccountEvent evt = new AccountPropertyUpdateEvent(this, property, value);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        String previousValue = get(property);
        
        // Check if existing value is the same as the one to be updated to.
        if ((previousValue != null && previousValue.equals(value))
                || (previousValue == null && value == null))
        {
            return true;
        }
        
        table.update(new WhereClause[]{
            new WhereClause("logit.accounts.username", WhereClause.EQUAL, get("logit.accounts.username")),
        }, new SetClause[]{
            new SetClause(property, value),
        });
        
        data.put(property, value);
        
        notifyObservers(property);
        
        return true;
    }
    
    public int size()
    {
        return data.size();
    }
    
    public synchronized void addObserver(AccountObserver o)
    {
        if (o == null)
            throw new NullPointerException();
        
        if (!obs.contains(o))
        {
            obs.addElement(o);
        }
    }
    
    public synchronized void deleteObserver(AccountObserver o)
    {
        obs.removeElement(o);
    }
    
    public synchronized void deleteObservers()
    {
        obs.removeAllElements();
    }
    
    public synchronized int countObservers()
    {
        return obs.size();
    }
    
    protected final void notifyObservers(String property)
    {
        Object[] observers;
        
        synchronized (this)
        {
            observers = obs.toArray(); 
        }
        
        for (int i = 0; i < observers.length; i++)
        {
            ((AccountObserver) observers[i]).update(this, property);
        }
    }
    
    private final Map<String, String> data;
    private final Table table;
    private final Vector<AccountObserver> obs = new Vector<>();
}
