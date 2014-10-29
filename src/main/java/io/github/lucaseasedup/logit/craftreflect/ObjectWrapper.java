package io.github.lucaseasedup.logit.craftreflect;

import io.github.lucaseasedup.logit.LogItPlugin;

public abstract class ObjectWrapper
{
    public ObjectWrapper(Object o)
    {
        this.holder = new ObjectHolder(o);
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
    
    public final ObjectHolder getHolder()
    {
        return holder;
    }
    
    public static final class ObjectHolder
    {
        private ObjectHolder(Object o)
        {
            this.o = o;
        }
        
        public Object get()
        {
            return o;
        }
        
        private void set(Object o)
        {
            this.o = o;
        }
        
        private Object o = null;
    }
    
    private final ObjectHolder holder;
}
