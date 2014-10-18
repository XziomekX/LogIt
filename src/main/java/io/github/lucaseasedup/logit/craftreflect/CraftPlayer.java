package io.github.lucaseasedup.logit.craftreflect;

public abstract class CraftPlayer extends ObjectWrapper
{
    protected CraftPlayer(Object o)
    {
        super(o);
    }
    
    public abstract EntityPlayer getHandle();
}
