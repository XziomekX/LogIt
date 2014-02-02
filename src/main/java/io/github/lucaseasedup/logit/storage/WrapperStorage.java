/*
 * WrapperStorage.java
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
package io.github.lucaseasedup.logit.storage;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

public final class WrapperStorage extends Storage
{
    public WrapperStorage(Storage leading)
    {
        this.leading = leading;
        this.mirrors = new Hashtable<>(5);
        this.obs = new Vector<>();
    }
    
    @Override
    public void connect() throws IOException
    {
        leading.connect();
        
        for (Storage storage : mirrors.keySet())
        {
            storage.connect();
        }
    }
    
    @Override
    public boolean isConnected() throws IOException
    {
        return leading.isConnected();
    }
    
    @Override
    public void ping() throws IOException
    {
        leading.ping();
        
        for (Storage storage : mirrors.keySet())
        {
            storage.ping();
        }
    }
    
    @Override
    public void close() throws IOException
    {
        leading.close();
        
        for (Storage storage : mirrors.keySet())
        {
            storage.close();
        }
    }
    
    @Override
    public List<String> getUnitNames() throws IOException
    {
        return leading.getUnitNames();
    }
    
    @Override
    public Hashtable<String, Type> getKeys(String unit) throws IOException
    {
        return leading.getKeys(unit);
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit) throws IOException
    {
        return leading.selectEntries(unit);
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        return leading.selectEntries(unit, keys);
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit,
                                                         List<String> keys,
                                                         Selector selector) throws IOException
    {
        return leading.selectEntries(unit, keys, selector);
    }
    
    @Override
    public void createUnit(String unit, Hashtable<String, Type> keys) throws IOException
    {
        leading.createUnit(unit, keys);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().createUnit(unitMapping, keys);
        }
        
        for (StorageObserver o : obs)
        {
            o.createUnit(unit, keys);
        }
    }
    
    @Override
    public void renameUnit(String unit, String newName) throws IOException
    {
        leading.renameUnit(unit, newName);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getValue().remove(unit);
            e.getValue().put(newName, unitMapping);
        }
        
        for (StorageObserver o : obs)
        {
            o.renameUnit(unit, newName);
        }
    }
    
    @Override
    public void eraseUnit(String unit) throws IOException
    {
        leading.eraseUnit(unit);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().eraseUnit(unitMapping);
        }
        
        for (StorageObserver o : obs)
        {
            o.eraseUnit(unit);
        }
    }
    
    @Override
    public void removeUnit(String unit) throws IOException
    {
        leading.removeUnit(unit);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().removeUnit(unitMapping);
        }
        
        for (StorageObserver o : obs)
        {
            o.removeUnit(unit);
        }
    }
    
    @Override
    public void addKey(String unit, String key, Type type) throws IOException
    {
        leading.addKey(unit, key, type);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().addKey(unitMapping, key, type);
        }
        
        for (StorageObserver o : obs)
        {
            o.addKey(unit, key, type);
        }
    }
    
    @Override
    public void addEntry(String unit, Hashtable<String, String> pairs) throws IOException
    {
        leading.addEntry(unit, pairs);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().addEntry(unitMapping, pairs);
        }
        
        for (StorageObserver o : obs)
        {
            o.addEntry(unit, pairs);
        }
    }
    
    @Override
    public void updateEntries(String unit, Hashtable<String, String> pairs, Selector selector)
            throws IOException
    {
        leading.updateEntries(unit, pairs, selector);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().updateEntries(unitMapping, pairs, selector);
        }
        
        for (StorageObserver o : obs)
        {
            o.updateEntries(unit, pairs, selector);
        }
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
        leading.removeEntries(unit, selector);
        
        for (Entry<Storage, Hashtable<String, String>> e : mirrors.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().removeEntries(unitMapping, selector);
        }
        
        for (StorageObserver o : obs)
        {
            o.removeEntries(unit, selector);
        }
    }
    
    @Override
    public void executeBatch() throws IOException
    {
        leading.executeBatch();
        
        for (Storage storage : mirrors.keySet())
        {
            storage.executeBatch();
        }
    }
    
    @Override
    public void clearBatch() throws IOException
    {
        leading.clearBatch();
        
        for (Storage storage : mirrors.keySet())
        {
            storage.clearBatch();
        }
    }
    
    @Override
    public void setAutobatchEnabled(boolean status)
    {
        super.setAutobatchEnabled(status);
        
        for (Storage storage : mirrors.keySet())
        {
            storage.setAutobatchEnabled(status);
        }
    }
    
    public void mirrorStorage(Storage s, Hashtable<String, String> unitMappings)
    {
        if (s == null)
            throw new NullPointerException();
        
        if (!mirrors.contains(s))
        {
            mirrors.put(s, unitMappings);
        }
    }
    
    public void unmirrorStorage(StorageObserver o)
    {
        mirrors.remove(o);
    }
    
    public void unmirrorAll()
    {
        mirrors.clear();
    }
    
    public void addObserver(StorageObserver o)
    {
        if (o == null)
            throw new NullPointerException();
        
        if (!obs.contains(o))
        {
            obs.addElement(o);
        }
    }
    
    public void deleteObserver(StorageObserver o)
    {
        obs.removeElement(o);
    }
    
    public void deleteObservers()
    {
        obs.removeAllElements();
    }
    
    public int countObservers()
    {
        return obs.size();
    }
    
    public Storage getLeadingStorage()
    {
        return leading;
    }
    
    private String getUnitMapping(Hashtable<String, String> mappings, String unit)
    {
        String unitMapping = mappings.get(unit);
        
        if (unitMapping == null)
            return unit;
        
        return unitMapping;
    }
    
    private final Storage leading;
    private final Hashtable<Storage, Hashtable<String, String>> mirrors;
    private final Vector<StorageObserver> obs;
}
