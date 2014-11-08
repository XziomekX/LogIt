package io.github.lucaseasedup.logit.storage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public final class StorageEntry implements Iterable<StorageDatum>
{
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        for (StorageDatum datum : this)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            
            sb.append("\"");
            sb.append(datum.getKey());
            sb.append("\": \"");
            sb.append(datum.getValue());
            sb.append("\"");
        }
        
        return "Entry {" + sb.toString() + "}";
    }
    
    public String get(String key)
    {
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException();
        
        return backend.get(key);
    }
    
    public void put(String key, String value)
    {
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException();
        
        String oldValue;
        
        if (value == null)
        {
            oldValue = backend.put(key, "");
        }
        else
        {
            oldValue = backend.put(key, value);
        }
        
        if (oldValue == null || !oldValue.equals(value))
        {
            dirtyKeys.add(key);
        }
    }
    
    public Set<String> getKeys()
    {
        return backend.keySet();
    }
    
    public boolean containsKey(String key)
    {
        return get(key) != null;
    }
    
    public StorageEntry copy()
    {
        StorageEntry copy = new StorageEntry();
        
        copy.backend = new LinkedHashMap<>(backend);
        
        return copy;
    }
    
    public StorageEntry copyDirty()
    {
        StorageEntry copy = new StorageEntry();
        
        for (Map.Entry<String, String> e : backend.entrySet())
        {
            if (isKeyDirty(e.getKey()))
            {
                copy.backend.put(e.getKey(), e.getValue());
            }
        }
        
        return copy;
    }
    
    public boolean isKeyDirty(String key)
    {
        if (key == null)
            throw new IllegalArgumentException();
        
        return dirtyKeys.contains(key);
    }
    
    public void clearKeyDirty(String key)
    {
        if (key == null)
            throw new IllegalArgumentException();
        
        dirtyKeys.remove(key);
    }
    
    @Override
    public Iterator<StorageDatum> iterator()
    {
        return new DatumIterator();
    }
    
    public static List<StorageEntry> copyList(List<StorageEntry> entries)
    {
        if (entries == null)
            throw new IllegalArgumentException();
        
        return copyList(entries, new SelectorConstant(true));
    }
    
    public static List<StorageEntry> copyList(List<StorageEntry> entries,
                                               Selector selector)
    {
        if (entries == null || selector == null)
            throw new IllegalArgumentException();
        
        List<StorageEntry> copies = new LinkedList<>();
        
        for (StorageEntry entry : entries)
        {
            if (SqlUtils.resolveSelector(selector, entry))
            {
                copies.add(entry.copy());
            }
        }
        
        return copies;
    }
    
    public static List<StorageEntry> copyList(List<StorageEntry> entries,
                                               List<String> keys,
                                               Selector selector)
    {
        if (entries == null || keys == null || selector == null)
            throw new IllegalArgumentException();
        
        List<StorageEntry> copies = new LinkedList<>();
        
        for (StorageEntry entry : entries)
        {
            if (SqlUtils.resolveSelector(selector, entry))
            {
                StorageEntry.Builder copyBuilder =
                        new StorageEntry.Builder();
                
                for (StorageDatum datum : entry)
                {
                    if (keys == null || keys.contains(datum.getKey()))
                    {
                        copyBuilder.put(datum.getKey(), datum.getValue());
                    }
                }
                
                copies.add(copyBuilder.build());
            }
        }
        
        return copies;
    }
    
    public final class DatumIterator implements Iterator<StorageDatum>
    {
        public DatumIterator()
        {
            it = backend.entrySet().iterator();
        }
        
        @Override
        public boolean hasNext()
        {
            return it.hasNext();
        }
        
        @Override
        public StorageDatum next()
        {
            Map.Entry<String, String> el = it.next();
            
            return new StorageDatum(el.getKey(), el.getValue());
        }
        
        @Override
        public void remove()
        {
            it.remove();
        }
        
        private final Iterator<Map.Entry<String, String>> it;
    }
    
    public static final class Builder
    {
        public Builder put(String key, String value)
        {
            entry.put(key, value);
            
            return this;
        }
        
        public Builder putAll(StorageEntry sourceEntry)
        {
            for (StorageDatum datum : sourceEntry)
            {
                put(datum.getKey(), datum.getValue());
            }
            
            return this;
        }
        
        public StorageEntry build()
        {
            StorageEntry builtEntry = entry;
            
            entry = new StorageEntry();
            
            builtEntry.dirtyKeys.clear();
            
            return builtEntry;
        }
        
        private StorageEntry entry = new StorageEntry();
    }
    
    private Map<String, String> backend = new LinkedHashMap<>();
    private Set<String> dirtyKeys = new HashSet<>();
}
