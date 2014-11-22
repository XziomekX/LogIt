package io.github.lucaseasedup.logit.locale;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.HashMap;
import java.util.Map;

public final class LocaleManager extends LogItCoreObject
{
    public Locale getActiveLocale()
    {
        return activeLocaleObj;
    }
    
    public void switchActiveLocale(Class<? extends Locale> locale)
    {
        if (locale == null)
            throw new IllegalArgumentException();
        
        Locale localeObj = locales.get(locale);
        
        if (localeObj == null)
        {
            if (fallbackLocaleObj == null)
                throw new RuntimeException("No fallback locale set.");
            
            localeObj = fallbackLocaleObj;
        }
        
        activeLocaleObj = localeObj;
    }
    
    public void switchActiveLocale(String tag)
    {
        if (tag == null)
            throw new IllegalArgumentException();
        
        Locale localeObj = getLocaleByTag(tag);
        
        if (localeObj == null)
        {
            if (fallbackLocaleObj == null)
                throw new RuntimeException("No fallback locale set");
            
            localeObj = fallbackLocaleObj;
        }
        
        activeLocaleObj = localeObj;
    }
    
    public Locale getFallbackLocale()
    {
        return fallbackLocaleObj;
    }
    
    /**
     * Sets the fallback locale, that will be activated
     * when trying to switch to a locale that has not been registered.
     * 
     * @param fallbackLocale the fallback locale.
     */
    public void setFallbackLocale(Class<? extends Locale> fallbackLocale)
    {
        if (fallbackLocale == null)
            throw new IllegalArgumentException();
        
        Locale fallbackLocaleObj = locales.get(fallbackLocale);
        
        if (fallbackLocaleObj == null)
            throw new RuntimeException("Locale not registered.");
        
        this.fallbackLocaleObj = fallbackLocaleObj;
    }
    
    public void registerLocale(Locale localeObj)
    {
        if (localeObj == null)
            throw new IllegalArgumentException();
        
        if (localeObj.getClass().getAnnotation(LocaleTag.class) == null)
            throw new RuntimeException("LocaleTag not found.");
        
        if (locales.containsKey(localeObj.getClass()))
            throw new RuntimeException("Locale already registered.");
        
        locales.put(localeObj.getClass(), localeObj);
    }
    
    public void unregisterLocale(Class<? extends Locale> locale)
    {
        if (locale == null)
            throw new IllegalArgumentException();
        
        if (!locales.containsKey(locale))
            throw new RuntimeException("Locale not registered.");
        
        locales.remove(locale);
    }
    
    /**
     * Returns locale from this {@code LocaleManager} that has the given tag.
     * 
     * @param tag the locale tag.
     * 
     * @return the locale object or {@code null} if no locale with this tag was found.
     */
    public Locale getLocaleByTag(String tag)
    {
        if (tag == null)
            throw new IllegalArgumentException();
        
        for (Map.Entry<Class<? extends Locale>, Locale> e : locales.entrySet())
        {
            if (getLocaleTag(e.getKey()).equals(tag))
            {
                return e.getValue();
            }
        }
        
        return null;
    }
    
    public static String getLocaleTag(Class<? extends Locale> locale)
    {
        if (locale == null)
            throw new IllegalArgumentException();
        
        return locale.getAnnotation(LocaleTag.class).value();
    }
    
    private final Map<Class<? extends Locale>, Locale> locales =
            new HashMap<>();
    private Locale activeLocaleObj;
    private Locale fallbackLocaleObj;
}
