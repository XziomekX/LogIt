package io.github.lucaseasedup.logit.cooldown;

import org.apache.commons.lang.StringUtils;

public final class Cooldown
{
    public Cooldown(String name)
    {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException();
        
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    private final String name;
}
