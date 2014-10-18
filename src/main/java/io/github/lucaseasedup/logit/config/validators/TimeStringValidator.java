package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;
import io.github.lucaseasedup.logit.config.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeStringValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null || !(value instanceof String))
            return false;
        
        Matcher matcher = PATTERN.matcher((String) value);
        
        if (!matcher.find())
            return false;
        
        do
        {
            String longValue = matcher.group(1);
            TimeUnit unit = TimeUnit.decode(matcher.group(2));
            
            if (unit == null)
                return false;
            
            try
            {
                Long.parseLong(longValue);
            }
            catch (NumberFormatException ex)
            {
                return false;
            }
        }
        while (matcher.find());
        
        return true;
    }
    
    public static final Pattern PATTERN =
            Pattern.compile("(?<=^|[A-Za-z]\\s)\\s*([0-9]+)\\s*([A-Za-z]+)\\s*(?=\\s|$)",
                    Pattern.MULTILINE);
}
