package io.github.lucaseasedup.logit.storage;

import java.util.List;

public final class PreloadedUnitCache
{
    public PreloadedUnitCache(
            UnitKeys keys, String primaryKey, List<StorageEntry> entries
    )
    {
        if (keys == null || primaryKey == null || entries == null)
            throw new IllegalArgumentException();
        
        this.keys = keys;
        this.primaryKey = primaryKey;
        this.entries = entries;
    }
    
    public UnitKeys getKeys()
    {
        return keys;
    }
    
    public String getPrimaryKey()
    {
        return primaryKey;
    }
    
    public List<StorageEntry> getEntryList()
    {
        return entries;
    }
    
    private final UnitKeys keys;
    private final String primaryKey;
    private final List<StorageEntry> entries;
}
