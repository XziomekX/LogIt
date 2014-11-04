package io.github.lucaseasedup.logit.common;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MaintainableHashMap<K, V> extends LinkedHashMap<K, V>
{
    public MaintainableHashMap(int maxEntries)
    {
        if (maxEntries <= 0)
            throw new IllegalArgumentException();
        
        this.maxEntries = maxEntries;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        return size() > maxEntries;
    }
    
    public int getMaxEntries()
    {
        return maxEntries;
    }
    
    private static final long serialVersionUID = 1L;
    
    private final int maxEntries;
}
