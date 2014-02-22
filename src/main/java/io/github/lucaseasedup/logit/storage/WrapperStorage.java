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

import io.github.lucaseasedup.logit.util.SqlUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public final class WrapperStorage extends Storage
{
    public WrapperStorage(Storage leading, CacheType cacheType)
    {
        this.leading = leading;
        this.cacheType = cacheType;
        
        mirrors = new HashSet<>(5);
        unitMappings = new Hashtable<>(5);
        obs = new Vector<>();
        
        if (cacheType == CacheType.PRELOADED)
        {
            preloadedCache = new Hashtable<>();
        }
    }
    
    @Override
    public void connect() throws IOException
    {
        leading.connect();
        
        for (Storage storage : mirrors)
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
        
        for (Storage storage : mirrors)
        {
            storage.ping();
        }
    }
    
    @Override
    public void close() throws IOException
    {
        leading.close();
        
        for (Storage storage : mirrors)
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
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            List<Hashtable<String, String>> preloadedUnit = preloadedCache.get(unit);
            
            if (preloadedUnit == null || preloadedUnit.isEmpty())
            {
                preloadedCache.put(unit, leading.selectEntries(unit));
            }
            
            List<Hashtable<String, String>> result = new LinkedList<>();
            
            for (Hashtable<String, String> entry : preloadedCache.get(unit))
            {
                result.add(new Hashtable<>(entry));
            }
            
            return result;
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, keys);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyCacheResultList(preloadedCache.get(unit), keys,
                        new SelectorConstant(true));
            }
            else
            {
                return leading.selectEntries(unit, keys);
            }
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit,
                                                         List<String> keys,
                                                         Selector selector) throws IOException
    {
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, keys, selector);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyCacheResultList(preloadedCache.get(unit), keys, selector);
            }
            else
            {
                return leading.selectEntries(unit, keys, selector);
            }
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public void createUnit(String unit, Hashtable<String, Type> keys) throws IOException
    {
        leading.createUnit(unit, keys);
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().createUnit(unitMapping, keys);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (!preloadedCache.containsKey(unit))
            {
                preloadedCache.put(unit, new LinkedList<Hashtable<String, String>>());
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.createUnit(unit, keys);
        }
    }
    
    @Override
    public void renameUnit(String unit, String newName) throws IOException
    {
        if (unit.equals(newName))
            throw new IllegalArgumentException();
        
        leading.renameUnit(unit, newName);
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getValue().remove(unit);
            e.getValue().put(newName, unitMapping);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.put(newName, preloadedCache.remove(unit));
            }
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
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().eraseUnit(unitMapping);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.get(unit).clear();
            }
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
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().removeUnit(unitMapping);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.remove(unit);
            }
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
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().addKey(unitMapping, key, type);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                for (Hashtable<String, String> entry : preloadedCache.get(unit))
                {
                    entry.put(key, "");
                }
            }
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
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().addEntry(unitMapping, pairs);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                Hashtable<String, String> newEntry = new Hashtable<>();
                
                for (Entry<String, String> pair : pairs.entrySet())
                {
                    newEntry.put(pair.getKey(), pair.getValue());
                }
                
                preloadedCache.get(unit).add(newEntry);
            }
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
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().updateEntries(unitMapping, pairs, selector);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                for (Hashtable<String, String> entry : preloadedCache.get(unit))
                {
                    if (SqlUtils.resolveSelector(selector, entry))
                    {
                        for (Entry<String, String> pair : pairs.entrySet())
                        {
                            entry.put(pair.getKey(), pair.getValue());
                        }
                    }
                }
            }
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
        
        for (Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().removeEntries(unitMapping, selector);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                Iterator<Hashtable<String, String>> it = preloadedCache.get(unit).iterator();
                
                while (it.hasNext())
                {
                    Hashtable<String, String> entry = it.next();
                    
                    if (SqlUtils.resolveSelector(selector, entry))
                    {
                        it.remove();
                    }
                }
            }
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
        
        for (Storage storage : mirrors)
        {
            storage.executeBatch();
        }
    }
    
    @Override
    public void clearBatch() throws IOException
    {
        leading.clearBatch();
        
        for (Storage storage : mirrors)
        {
            storage.clearBatch();
        }
    }
    
    @Override
    public void setAutobatchEnabled(boolean status)
    {
        super.setAutobatchEnabled(status);
        
        for (Storage storage : mirrors)
        {
            storage.setAutobatchEnabled(status);
        }
    }
    
    public void mirrorStorage(Storage storage, Hashtable<String, String> unitMappings)
    {
        if (storage == null)
            throw new NullPointerException();
        
        if (!mirrors.contains(storage))
        {
            mirrors.add(storage);
        }
        
        if (!unitMappings.containsKey(storage))
        {
            this.unitMappings.put(storage, unitMappings);
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
    
    private List<Hashtable<String, String>> copyCacheResultList(List<Hashtable<String, String>> result,
                                                                List<String> keys,
                                                                Selector selector)
    {
        List<Hashtable<String, String>> resultCopy = new LinkedList<>();
        
        for (Hashtable<String, String> entry : result)
        {
            if (SqlUtils.resolveSelector(selector, entry))
            {
                Hashtable<String, String> resultEntry = new Hashtable<>();
                
                for (Entry<String, String> e : entry.entrySet())
                {
                    if (keys.contains(e.getKey()))
                    {
                        resultEntry.put(e.getKey(), e.getValue());
                    }
                }
                
                for (String key : keys)
                {
                    if (!resultEntry.containsKey(key))
                    {
                        resultEntry.put(key, "");
                    }
                }
                
                resultCopy.add(resultEntry);
            }
        }
        
        return resultCopy;
    }
    
    private final Storage leading;
    private final CacheType cacheType;
    
    private final Set<Storage> mirrors;
    private final Hashtable<Storage, Hashtable<String, String>> unitMappings;
    private final Vector<StorageObserver> obs;
    
    private Hashtable<String, List<Hashtable<String, String>>> preloadedCache;
}
