package io.github.lucaseasedup.logit.locale;

@LocaleTag("de")
public final class GermanLocale implements Locale
{
    private GermanLocale()
    {
    }
    
    @Override
    public String stringifySeconds(long seconds)
    {
        if (seconds < 0)
            throw new IllegalArgumentException();
        
        if (seconds == 0)
            return "0 Sekunden";
        
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
            sb.append(" Tag");
            
            if (days > 1)
            {
                sb.append("e");
            }
            
            if (hours > 0 || minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (hours > 0)
        {
            sb.append(hours);
            sb.append(" Stunde");
            
            if (hours > 1)
            {
                sb.append("n");
            }
            
            if (minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (minutes > 0)
        {
            sb.append(minutes);
            sb.append(" Minute");
            
            if (minutes > 1)
            {
                sb.append("n");
            }
            
            if (seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (seconds > 0)
        {
            sb.append(seconds);
            sb.append(" Sekunde");
            
            if (seconds > 1)
            {
                sb.append("n");
            }
        }
        
        return sb.toString();
    }
    
    public static GermanLocale getInstance()
    {
        return INSTANCE;
    }
    
    private static final GermanLocale INSTANCE = new GermanLocale();
}
