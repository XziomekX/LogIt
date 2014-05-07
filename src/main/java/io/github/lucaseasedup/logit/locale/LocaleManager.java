/*
 * LocaleManager.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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
package io.github.lucaseasedup.logit.locale;

import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class LocaleManager extends LogItCoreObject implements Disposable
{
    @Override
    public void dispose()
    {
        activeLocaleObj = null;
        fallbackLocaleObj = null;
        
        locales.clear();
        locales = null;
    }
    
    public Locale getActiveLocale()
    {
        return activeLocaleObj;
    }
    
    public void switchActiveLocale(Class<? extends Locale> locale)
    {
        Locale localeObj = locales.get(locale);
        
        if (localeObj == null)
        {
            if (fallbackLocaleObj == null)
                throw new RuntimeException("No fallback locale set.");
            
            localeObj = fallbackLocaleObj;
        }
        
        activeLocaleObj = localeObj;
    }
    
    public void switchActiveLocale(String prefix)
    {
        Locale localeObj = getLocaleByPrefix(prefix);
        
        if (localeObj == null)
        {
            if (fallbackLocaleObj == null)
                throw new RuntimeException("No fallback locale set.");
            
            localeObj = fallbackLocaleObj;
        }
        
        activeLocaleObj = localeObj;
    }
    
    public Locale getFallbackLocale()
    {
        return fallbackLocaleObj;
    }
    
    public void setFallbackLocale(Class<? extends Locale> fallbackLocale)
    {
        Locale fallbackLocaleObj = locales.get(fallbackLocale);
        
        if (fallbackLocaleObj == null)
            throw new RuntimeException("Locale not registered.");
        
        this.fallbackLocaleObj = fallbackLocaleObj;
    }
    
    public void registerLocale(Locale localeObj)
    {
        if (localeObj.getClass().getAnnotation(LocalePrefix.class) == null)
            throw new RuntimeException("LocalePrefix not found.");
        
        if (locales.containsKey(localeObj.getClass()))
            throw new RuntimeException("Locale already registered.");
        
        locales.put(localeObj.getClass(), localeObj);
    }
    
    public void unregisterLocale(Class<? extends Locale> locale)
    {
        if (!locales.containsKey(locale))
            throw new RuntimeException("Locale not registered.");
        
        locales.remove(locale);
    }
    
    public Locale getLocaleByPrefix(String prefix)
    {
        for (Entry<Class<? extends Locale>, Locale> e : locales.entrySet())
        {
            if (getLocalePrefix(e.getKey()).equals(prefix))
            {
                return e.getValue();
            }
        }
        
        return null;
    }
    
    public static String getLocalePrefix(Class<? extends Locale> locale)
    {
        return locale.getAnnotation(LocalePrefix.class).value();
    }
    
    private Map<Class<? extends Locale>, Locale> locales = new HashMap<>();
    private Locale activeLocaleObj;
    private Locale fallbackLocaleObj;
}
