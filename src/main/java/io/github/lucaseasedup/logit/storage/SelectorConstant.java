package io.github.lucaseasedup.logit.storage;

public final class SelectorConstant extends Selector
{
    public SelectorConstant(boolean value)
    {
        this.value = value;
    }
    
    public boolean getValue()
    {
        return value;
    }
    
    private final boolean value;
}
