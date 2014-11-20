package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.config.validators.TimeStringValidator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public final class TimeString
{
    private TimeString()
    {
    }
    
    public static long decode(String string, TimeUnit convertTo)
    {
        if (string == null || convertTo == null)
            throw new IllegalArgumentException();
        
        CacheTag cacheTag = new CacheTag(string, convertTo);
        Long cachedResult = cache.get(cacheTag);
        
        if (cachedResult != null)
        {
            return cachedResult;
        }
        
        Matcher matcher = TimeStringValidator.PATTERN.matcher(string);
        long time = 0;
        
        while (matcher.find())
        {
            String longValue = matcher.group(1);
            TimeUnit unit = TimeUnit.decode(matcher.group(2));
            
            if (unit != null)
            {
                time += unit.convertTo(Long.parseLong(longValue), convertTo);
            }
        }
        
        cache.put(cacheTag, time);
        
        return time;
    }
    
    private static final class CacheTag
    {
        private CacheTag(String string, TimeUnit convertTo)
        {
            if (string == null || convertTo == null)
                throw new IllegalArgumentException();
            
            this.string = string;
            this.convertTo = convertTo;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof CacheTag))
                return false;
            
            CacheTag cacheTag = (CacheTag) obj;
            
            return cacheTag.string.equals(string)
                    && cacheTag.convertTo.equals(convertTo);
        }
        
        @Override
        public int hashCode()
        {
            int result = string.hashCode();
            result = 31 * result + convertTo.hashCode();
            return result;
        }
        
        private final String string;
        private final TimeUnit convertTo;
    }
    
    private static final Map<CacheTag, Long> cache = new HashMap<>();
}
