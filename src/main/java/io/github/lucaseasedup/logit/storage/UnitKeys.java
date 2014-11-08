package io.github.lucaseasedup.logit.storage;

import java.util.LinkedHashMap;

public class UnitKeys extends LinkedHashMap<String, DataType>
{
    @Override
    public DataType put(String key, DataType value)
    {
        if (key == null || key.isEmpty() || value == null)
            throw new IllegalArgumentException();
        
        return super.put(key, value);
    }
    
    private static final long serialVersionUID = 1L;
}
