/*
 * ObjectWrapper.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.craftreflect;

import io.github.lucaseasedup.logit.LogItPlugin;

public abstract class ObjectWrapper
{
    public ObjectWrapper(Object o)
    {
        this.holder = new ObjectHolder(o);
    }
    
    public final <T extends ObjectWrapper> T cast(Class<T> castTo) throws ReflectiveOperationException
    {
        String bukkitVersion = LogItPlugin.getCraftBukkitVersion();
        
        String wrapperCraftClassName =
                "io.github.lucaseasedup.logit.craftreflect." + bukkitVersion + "." + castTo.getSimpleName();
        Class<?> wrapperCraftClass = Class.forName(wrapperCraftClassName);
        
        @SuppressWarnings("unchecked")
        T newWrapper = (T) wrapperCraftClass.getConstructor().newInstance();
        
        String objectCraftClassName =
                holder.get().getClass().getName().replace(holder.get().getClass().getSimpleName(), castTo.getSimpleName());
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
