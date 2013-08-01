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
        
        @SuppressWarnings("unchecked")
        T2 newWrapper = (T2) wrapperCraftClass.getConstructor().newInstance();
        
        Class<?> objectCraftClass =
            Class.forName(o.getClass().getName().replace(o.getClass().getSimpleName(), castTo.getSimpleName()));
        newWrapper.o = objectCraftClass.cast(o);
        
        return newWrapper;
    }
    
    public Object o = null;
}
