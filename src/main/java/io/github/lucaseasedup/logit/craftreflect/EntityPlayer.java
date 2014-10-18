package io.github.lucaseasedup.logit.craftreflect;

public abstract class EntityPlayer extends ObjectWrapper
{
    protected EntityPlayer(Object o)
    {
        super(o);
    }
    
    public abstract int getPing();
}
