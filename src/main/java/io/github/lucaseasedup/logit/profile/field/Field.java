package io.github.lucaseasedup.logit.profile.field;

import org.apache.commons.lang.StringUtils;

public abstract class Field
{
    public Field(String name)
    {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException();
        
        this.name = name;
    }
    
    public final String getName()
    {
        return name;
    }
    
    private final String name;
}
