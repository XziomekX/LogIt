package io.github.lucaseasedup.logit.config;

public enum TimeUnit
{
    MILLISECONDS(1), TICKS(50), SECONDS(1000), MINUTES(60000),
    HOURS(3600000), DAYS(86400000), WEEKS(604800000);
    
    private TimeUnit(long milliseconds)
    {
        if (milliseconds < 0)
            throw new IllegalArgumentException();
        
        this.milliseconds = milliseconds;
    }
    
    public long getMilliseconds()
    {
        return milliseconds;
    }
    
    public long convert(long value, TimeUnit to)
    {
        if (value < 0 || to == null)
            throw new IllegalArgumentException();
        
        if (value == 0)
            return 0;
        
        return value * milliseconds / to.milliseconds;
    }
    
    /**
     * Decodes a string into {@code TimeUnit}.
     * 
     * @param s the string to be decoded.
     * 
     * @return the decoded {@code TimeUnit} or {@code null} if the string could not be decoded.
     */
    public static TimeUnit decode(String s)
    {
        if (s == null)
            throw new IllegalArgumentException();
        
        switch (s.toLowerCase())
        {
        case "ms":
        case "millis":
        case "millisecond":
        case "milliseconds":
            return MILLISECONDS;
            
        case "tick":
        case "ticks":
            return TICKS;
            
        case "s":
        case "sec":
        case "secs":
        case "second":
        case "seconds":
            return SECONDS;
            
        case "min":
        case "mins":
        case "minute":
        case "minutes":
            return MINUTES;
            
        case "h":
        case "hr":
        case "hrs":
        case "hour":
        case "hours":
            return HOURS;
            
        case "d":
        case "day":
        case "days":
            return DAYS;
            
        case "w":
        case "wk":
        case "week":
        case "weeks":
            return WEEKS;
        }
        
        return null;
    }
    
    private final long milliseconds;
}
