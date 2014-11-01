package io.github.lucaseasedup.logit.config;

public enum TimeUnit
{
    /*
     * 1 ms = 1 ms
     */
    MILLISECONDS(1),
    
    /*
     * 1000 ms = 20 ticks
     * 1 tick = 1000 / 20 ms = 50 ms
     */
    TICKS(50),
    
    /*
     * 1 s = 1000 ms
     */
    SECONDS(1000),
    
    /*
     * 1 min = 60 * 1000 ms
     */
    MINUTES(60000),
    
    /*
     * 1 hour = 60 * 60 * 1000 ms
     */
    HOURS(3600000),
    
    /*
     * 1 day = 24 * 60 * 60 * 1000 ms
     */
    DAYS(86400000),
    
    /*
     * 1 week = 7 * 24 * 60 * 60 * 1000 ms
     */
    WEEKS(604800000);
    
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
    
    public long convertTo(long value, TimeUnit to)
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
