package io.github.lucaseasedup.logit.storage;

import org.apache.commons.lang.StringUtils;

public final class StorageDatum
{
    /* package */ StorageDatum(String key, String value)
    {
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException();
        
        this.key = key;
        
        if (value == null)
        {
            this.value = "";
        }
        else
        {
            this.value = value;
        }
    }
    
    public String getKey()
    {
        return key;
    }
    
    public String getValue()
    {
        return value;
    }
    
    private final String key;
    private final String value;
}
