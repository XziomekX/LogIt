package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.storage.Storage.DataType;
import java.util.Hashtable;
import java.util.List;

public final class PreloadedUnitCache
{
    public PreloadedUnitCache(Hashtable<String, DataType> keys,
                              String primaryKey,
                              List<Storage.Entry> entries)
    {
        if (keys == null || primaryKey == null || entries == null)
            throw new IllegalArgumentException();
        
        this.keys = keys;
        this.primaryKey = primaryKey;
        this.entries = entries;
    }
    
    public Hashtable<String, DataType> getKeys()
    {
        return keys;
    }
    
    public String getPrimaryKey()
    {
        return primaryKey;
    }
    
    public List<Storage.Entry> getEntryList()
    {
        return entries;
    }
    
    private final Hashtable<String, DataType> keys;
    private final String primaryKey;
    private final List<Storage.Entry> entries;
}
