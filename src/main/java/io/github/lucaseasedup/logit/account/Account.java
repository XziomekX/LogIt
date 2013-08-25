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

import com.google.common.collect.ImmutableSet;
import io.github.lucaseasedup.logit.IniFile;
import io.github.lucaseasedup.logit.db.SetClause;
import io.github.lucaseasedup.logit.db.Table;
import io.github.lucaseasedup.logit.db.WhereClause;
import it.sauronsoftware.base64.Base64;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import org.bukkit.Bukkit;

/**
 * @author LucasEasedUp
 */
public final class Account extends Observable
{
    public Account(Table table, Map<String, String> initialData) throws SQLException
    {
        this.data = new HashMap<>(initialData);
        this.table = table;
        
        refreshPersistence();
        savePersistence();
    }
    
    public Set<String> getProperties()
    {
        return ImmutableSet.copyOf(data.keySet());
    }
    
    public String getString(String property)
    {
        return data.get(property);
    }
    
    public int getInt(String property)
    {
        return Integer.parseInt(getString(property));
    }

    public long getLong(String property)
    {
        return Long.parseLong(getString(property));
    }
    
    public boolean updateString(String property, String value) throws SQLException
    {
        AccountEvent evt = new AccountPropertyUpdateEvent(this, property, value);
        
        Bukkit.getPluginManager().callEvent(evt);
        
        if (evt.isCancelled())
            return false;
        
        String previousValue = getString(property);
        
        // Check if existing value is the same as the one to be updated to.
        if ((previousValue != null && previousValue.equals(value))
                || (previousValue == null && value == null))
        {
            return true;
        }
        
        table.update(new WhereClause[]{
            new WhereClause("logit.accounts.username",
                    WhereClause.EQUAL, getString("logit.accounts.username")),
        }, new SetClause[]{
            new SetClause(property, value),
        });
        
        data.put(property, value);
        
        notifyObservers(property);
        
        return true;
    }
    
    public boolean updateInt(String property, int value) throws SQLException
    {
        return updateString(property, String.valueOf(value));
    }

    public boolean updateLong(String property, long value) throws SQLException
    {
        return updateString(property, String.valueOf(value));
    }
    
    public String getPersistence(String key)
    {
        if (table.isColumnDisabled("logit.accounts.persistence"))
            return null;
        
        return persistence.get(key);
    }
    
    public void updatePersistence(String key, String value) throws SQLException
    {
        if (table.isColumnDisabled("logit.accounts.persistence"))
            return;
        
        persistence.put(key, value);
        
        savePersistence();
    }
    
    private void refreshPersistence() throws SQLException
    {
        if (table.isColumnDisabled("logit.accounts.persistence"))
            return;
        
        persistence = new LinkedHashMap<>();
        
        String persistanceBase64String = getString("logit.accounts.persistence");
        IniFile iniFile;
        
        if (persistanceBase64String != null)
        {
            String persistenceString = Base64.decode(persistanceBase64String);
            iniFile = new IniFile(persistenceString);
        }
        else
        {
            iniFile = new IniFile();
        }
        
        if (!iniFile.hasSection("persistence"))
        {
            iniFile.putSection("persistence");
        }
        
        for (String key : iniFile.getSectionKeys("persistence"))
        {
            persistence.put(key, iniFile.getString("persistence", key));
        }
    }
    
    private void savePersistence() throws SQLException
    {
        if (table.isColumnDisabled("logit.accounts.persistence"))
            return;
        
        IniFile iniFile = new IniFile();
        iniFile.putSection("persistence");
        
        for (Entry<String, String> kv : persistence.entrySet())
        {
            iniFile.putString("persistence", kv.getKey(), kv.getValue());
        }
        
        updateString("logit.accounts.persistence", Base64.encode(iniFile.toString()));
    }
    
    private final Map<String, String> data;
    private Map<String, String> persistence = null;
    private final Table table;
}
