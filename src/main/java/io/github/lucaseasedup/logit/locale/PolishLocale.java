package io.github.lucaseasedup.logit.locale;

@LocalePrefix("pl")
public final class PolishLocale implements Locale
{
    private PolishLocale()
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
            
            if (days == 1)
            {
                sb.append(" dzien");
            }
            else
            {
                sb.append(" dni");
            }
            
            if (hours > 0 || minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (hours > 0)
        {
            sb.append(hours);
            
            if (hours == 1)
            {
                sb.append(" godzina");
            }
            else if (hours >= 2 && hours <= 4)
            {
                sb.append(" godziny");
            }
            else
            {
                sb.append(" godzin");
            }
            
            if (minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (minutes > 0)
        {
            sb.append(minutes);
            
            if (minutes == 1)
            {
                sb.append(" minuta");
            }
            else if (minutes >= 2 && minutes <= 4)
            {
                sb.append(" minuty");
            }
            else
            {
                sb.append(" minut");
            }
            
            if (seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (seconds > 0)
        {
            sb.append(seconds);
            
            if (minutes == 1)
            {
                sb.append(" sekunda");
            }
            else if (minutes >= 2 && minutes <= 4)
            {
                sb.append(" sekundy");
            }
            else
            {
                sb.append(" sekund");
            }
        }
        
        return sb.toString();
    }
    
    public static PolishLocale getInstance()
    {
        return INSTANCE;
    }
    
    private static final PolishLocale INSTANCE = new PolishLocale();
}
