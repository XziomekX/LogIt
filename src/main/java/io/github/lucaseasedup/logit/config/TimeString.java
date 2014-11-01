package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.config.validators.TimeStringValidator;
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
        
        return time;
    }
}
