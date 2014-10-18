package io.github.lucaseasedup.logit.locale;

@LocalePrefix("en")
public final class EnglishLocale implements Locale
{
    private EnglishLocale()
    {
    }
    
    @Override
    public String stringifySeconds(long seconds)
    {
        StringBuilder sb = new StringBuilder();
        long days, hours, minutes;
        
        days = seconds / 86400L;
        seconds %= 86400;
        hours = seconds / 3600L;
        seconds %= 3600;
        minutes = seconds / 60L;
        seconds %= 60;
        
        if (days > 0)
        {
            sb.append(days);
            sb.append(" day");
            
            if (days > 1)
            {
                sb.append("s");
            }
            
            if (hours > 0 || minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (hours > 0)
        {
            sb.append(hours);
            sb.append(" hour");
            
            if (hours > 1)
            {
                sb.append("s");
            }
            
            if (minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (minutes > 0)
        {
            sb.append(minutes);
            sb.append(" minute");
            
            if (minutes > 1)
            {
                sb.append("s");
            }
            
            if (seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (seconds > 0)
        {
            sb.append(seconds);
            sb.append(" second");
            
            if (seconds > 1)
            {
                sb.append("s");
            }
        }
        
        return sb.toString();
    }
    
    public static EnglishLocale getInstance()
    {
        return INSTANCE;
    }
    
    private static final EnglishLocale INSTANCE = new EnglishLocale();
}
