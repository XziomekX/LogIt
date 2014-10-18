package io.github.lucaseasedup.logit.profile.field;

import java.util.Collection;

public final class SetField extends Field
{
    public SetField(String name, Collection<String> values)
    {
        super(name);
        
        if (values == null)
            throw new IllegalArgumentException();
        
        this.values = values;
    }
    
    public boolean isAccepted(String value)
    {
        for (String s : values)
        {
            if ((s == null && value == null) || (s != null && s.equals(value)))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public Collection<String> getAcceptedValues()
    {
        return values;
    }
    
    private final Collection<String> values;
}
