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

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.HashSet;
import java.util.Set;

public final class LocaleManager extends LogItCoreObject
{
    public Locale getActiveLocale()
    {
        return activeLocale;
    }
    
    public void switchActiveLocale(Locale locale)
    {
        activeLocale = locale;
    }
    
    public Locale getFallbackLocale()
    {
        return fallbackLocale;
    }
    
    public void setFallbackLocale(Locale fallbackLocale)
    {
        this.fallbackLocale = fallbackLocale;
    }
    
    public void registerLocale(Locale locale)
    {
        if (locale.getClass().getAnnotation(LocalePrefix.class) == null)
            throw new IllegalArgumentException();
        
        locales.add(locale);
    }
    
    public void unregisterLocale(Locale locale)
    {
        locales.remove(locale);
    }
    
    public String getLocalePrefix(Locale locale)
    {
        if (!locales.contains(locale))
            throw new IllegalArgumentException();
        
        return locale.getClass().getAnnotation(LocalePrefix.class).value();
    }
    
    public Locale getLocale(String prefix)
    {
        for (Locale locale : locales)
        {
            if (locale.getClass().getAnnotation(LocalePrefix.class).value().equals(prefix))
            {
                return locale;
            }
        }
        
        return null;
    }
    
    private final Set<Locale> locales = new HashSet<>();
    private Locale activeLocale = null;
    private Locale fallbackLocale = null;
}
