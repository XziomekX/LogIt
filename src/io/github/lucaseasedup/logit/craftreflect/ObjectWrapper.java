package io.github.lucaseasedup.logit.craftreflect;

import io.github.lucaseasedup.logit.LogItPlugin;

/**
 * @author LucasEasedUp
 */
public abstract class ObjectWrapper
{
    public <T2 extends ObjectWrapper> T2 cast(Class<T2> castTo) throws ReflectiveOperationException
    {
        String version = LogItPlugin.getCraftBukkitVersion();
        
        Class<?> wrapperCraftClass =
            Class.forName("io.github.lucaseasedup.logit.craftreflect." + version + "." + castTo.getSimpleName());
        T2 newWrapper = (T2) wrapperCraftClass.getConstructor().newInstance();
        
        Class<?> objectCraftClass =
            Class.forName(o.getClass().getName().replace(o.getClass().getSimpleName(), castTo.getSimpleName()));
        newWrapper.o = objectCraftClass.cast(o);
        
        return newWrapper;
    }
    
    public Object o = null;
}
