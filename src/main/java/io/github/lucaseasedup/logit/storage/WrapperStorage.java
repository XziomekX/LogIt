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

import io.github.lucaseasedup.logit.util.CollectionUtils;
import io.github.lucaseasedup.logit.util.SqlUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public final class WrapperStorage extends Storage
{
    private WrapperStorage(Storage leading,
                           List<String> keys,
                           String indexKey,
                           CacheType cacheType)
    {
        this.leading = leading;
        this.keys = keys;
        this.indexKey = indexKey;
        this.cacheType = cacheType;
        
        if (cacheType == CacheType.PRELOADED)
        {
            preloadedCache = new Hashtable<>();
        }
    }
    
    @Override
    public void connect() throws IOException
    {
        leading.connect();
        
        for (Storage mirror : mirrors)
        {
            mirror.connect();
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
        
        for (Storage mirror : mirrors)
        {
            mirror.ping();
        }
    }
    
    @Override
    public void close() throws IOException
    {
        leading.close();
        
        for (Storage mirror : mirrors)
        {
            mirror.close();
        }
    }
    
    @Override
    public List<String> getUnitNames() throws IOException
    {
        return leading.getUnitNames();
    }
    
    @Override
    public Hashtable<String, DataType> getKeys(String unit) throws IOException
    {
        return leading.getKeys(unit);
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit) throws IOException
    {
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            List<Storage.Entry> preloadedEntries = preloadedCache.get(unit);
            
            if (preloadedEntries == null || preloadedEntries.isEmpty())
            {
                preloadedCache.put(unit, leading.selectEntries(unit));
            }
            
            List<Storage.Entry> entries = new LinkedList<>();
            
            for (Storage.Entry entry : preloadedCache.get(unit))
            {
                entries.add(entry.copy());
            }
            
            return entries;
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit, List<String> keys)
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
                return copyEntries(preloadedCache.get(unit), keys, new SelectorConstant(true));
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
    public List<Storage.Entry> selectEntries(String unit, Selector selector) throws IOException
    {
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, selector);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyEntries(preloadedCache.get(unit), null, selector);
            }
            else
            {
                return leading.selectEntries(unit, selector);
            }
        }
        else
        {
            throw new RuntimeException("Unknown cache type: " + cacheType);
        }
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit, List<String> keys, Selector selector)
            throws IOException
    {
        if (cacheType == CacheType.DISABLED)
        {
            return leading.selectEntries(unit, keys, selector);
        }
        else if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                return copyEntries(preloadedCache.get(unit), keys, selector);
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
    public void createUnit(String unit, Hashtable<String, DataType> keys) throws IOException
    {
        leading.createUnit(unit, keys);
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().createUnit(unitMapping, keys);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (!preloadedCache.containsKey(unit))
            {
                preloadedCache.put(unit, new LinkedList<Storage.Entry>());
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
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
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
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
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
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
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
    public void addKey(String unit, String key, DataType type) throws IOException
    {
        leading.addKey(unit, key, type);
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().addKey(unitMapping, key, type);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                for (Storage.Entry entry : preloadedCache.get(unit))
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
    public void addEntry(String unit, Storage.Entry entry) throws IOException
    {
        leading.addEntry(unit, entry);
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().addEntry(unitMapping, entry);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                preloadedCache.get(unit).add(entry.copy());
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.addEntry(unit, entry);
        }
    }
    
    @Override
    public void updateEntries(String unit, Storage.Entry entrySubset, Selector selector)
            throws IOException
    {
        leading.updateEntries(unit, entrySubset, selector);
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().updateEntries(unitMapping, entrySubset, selector);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                for (Storage.Entry entry : preloadedCache.get(unit))
                {
                    if (SqlUtils.resolveSelector(selector, entry))
                    {
                        for (Storage.Entry.Datum datum : entrySubset)
                        {
                            entry.put(datum.getKey(), datum.getValue());
                        }
                    }
                }
            }
        }
        
        for (StorageObserver o : obs)
        {
            o.updateEntries(unit, entrySubset, selector);
        }
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
        leading.removeEntries(unit, selector);
        
        for (Map.Entry<Storage, Hashtable<String, String>> e : unitMappings.entrySet())
        {
            String unitMapping = getUnitMapping(e.getValue(), unit);
            
            e.getKey().removeEntries(unitMapping, selector);
        }
        
        if (cacheType == CacheType.PRELOADED)
        {
            if (preloadedCache.containsKey(unit))
            {
                Iterator<Storage.Entry> it = preloadedCache.get(unit).iterator();
                
                while (it.hasNext())
                {
                    Storage.Entry entry = it.next();
                    
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
        
        for (Storage mirror : mirrors)
        {
            mirror.executeBatch();
        }
    }
    
    @Override
    public void clearBatch() throws IOException
    {
        leading.clearBatch();
        
        for (Storage mirror : mirrors)
        {
            mirror.clearBatch();
        }
    }
    
    @Override
    public void setAutobatchEnabled(boolean status)
    {
        super.setAutobatchEnabled(status);
        leading.setAutobatchEnabled(status);
        
        for (Storage mirror : mirrors)
        {
            mirror.setAutobatchEnabled(status);
        }
    }
    
    public void mirrorStorage(Storage storage, Hashtable<String, String> unitMappings)
    {
        if (storage == null)
            throw new IllegalArgumentException();
        
        if (!mirrors.contains(storage))
        {
            mirrors.add(storage);
        }
        
        if (!this.unitMappings.containsKey(storage))
        {
            this.unitMappings.put(storage, unitMappings);
        }
    }
    
    public void unmirrorStorage(Storage o)
    {
        mirrors.remove(o);
        unitMappings.remove(o);
    }
    
    public void unmirrorAll()
    {
        mirrors.clear();
        unitMappings.clear();
    }
    
    public void addObserver(StorageObserver o)
    {
        if (o == null)
            throw new IllegalArgumentException();
        
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
    
    private List<Storage.Entry> copyEntries(List<Storage.Entry> entries,
                                            List<String> keys,
                                            Selector selector)
    {
        List<Storage.Entry> copiedEntries = new LinkedList<>();
        
        for (Storage.Entry entry : entries)
        {
            if (SqlUtils.resolveSelector(selector, entry))
            {
                Storage.Entry copiedEntry = new Storage.Entry();
                
                for (Storage.Entry.Datum datum : entry)
                {
                    if (keys == null || keys.contains(datum.getKey()))
                    {
                        copiedEntry.put(datum.getKey(), datum.getValue());
                    }
                }
                
                copiedEntries.add(copiedEntry);
            }
        }
        
        return copiedEntries;
    }
    
    public static final class Builder
    {
        public WrapperStorage build()
        {
            return new WrapperStorage(leading, keys, indexKey, cacheType);
        }
        
        public Builder leading(Storage leading)
        {
            if (leading == null)
                throw new IllegalArgumentException();
            
            this.leading = leading;
            
            return this;
        }
        
        public Builder keys(List<String> keys)
        {
            if (keys == null)
                throw new IllegalArgumentException();
            
            if (CollectionUtils.containsDuplicates(keys))
                throw new IllegalArgumentException("All keys must be unique.");
            
            this.keys = keys;
            
            return this;
        }
        
        public Builder indexKey(String indexKey)
        {
            if (keys == null)
                throw new IllegalStateException("No keys defined.");
            
            if (indexKey != null && !keys.contains(indexKey))
                throw new IllegalArgumentException("Index key not found.");
            
            this.indexKey = indexKey;
            
            return this;
        }
        
        public Builder cacheType(CacheType cacheType)
        {
            if (cacheType == null)
                throw new IllegalArgumentException();
            
            this.cacheType = cacheType;
            
            return this;
        }
        
        private Storage leading;
        private List<String> keys;
        private String indexKey;
        private CacheType cacheType;
    }
    
    private final Storage leading;
    private final List<String> keys;
    private final String indexKey;
    private final CacheType cacheType;
    
    private final Set<Storage> mirrors = new HashSet<>();
    private final Hashtable<Storage, Hashtable<String, String>> unitMappings = new Hashtable<>();
    private final Vector<StorageObserver> obs = new Vector<>();
    
    private Hashtable<String, List<Storage.Entry>> preloadedCache;
}
