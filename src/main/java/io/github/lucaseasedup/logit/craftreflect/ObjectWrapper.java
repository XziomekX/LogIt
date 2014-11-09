package io.github.lucaseasedup.logit.craftreflect;

import io.github.lucaseasedup.logit.LogItPlugin;

public abstract class ObjectWrapper
{
    public ObjectWrapper(Object o)
    {
        this.holder = new Holder(o);
    }
    
    public final <T extends ObjectWrapper> T cast(Class<T> castTo)
            throws ReflectiveOperationException
    {
        String bukkitVersion = LogItPlugin.getCraftBukkitVersion();
        String wrapperCraftClassName = LogItPlugin.PACKAGE + ".craftreflect."
                    + bukkitVersion + "." + castTo.getSimpleName();
        Class<?> wrapperCraftClass = Class.forName(wrapperCraftClassName);
        
        @SuppressWarnings("unchecked")
        T newWrapper = (T) wrapperCraftClass.getConstructor().newInstance();
        
        String objectCraftClassName = holder.get().getClass().getName().replace(
                holder.get().getClass().getSimpleName(), castTo.getSimpleName()
        );
        Class<?> objectCraftClass = Class.forName(objectCraftClassName);
        
        newWrapper.getHolder().set(objectCraftClass.cast(holder.get()));
        
        return newWrapper;
    }
    
    public final Holder getHolder()
    {
        return holder;
    }
    
    public static final class Holder
    {
        private Holder(Object obj)
        {
            this.obj = obj;
        }
        
        public Object get()
        {
            return obj;
        }
        
        private void set(Object obj)
        {
            this.obj = obj;
        }
        
        private Object obj = null;
    }
    
    private final Holder holder;
}
