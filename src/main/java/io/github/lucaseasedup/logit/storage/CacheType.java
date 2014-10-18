package io.github.lucaseasedup.logit.storage;

public enum CacheType
{
    DISABLED("disabled"), PRELOADED("preloaded");
    
    private CacheType(String name)
    {
        if (name == null)
            throw new IllegalArgumentException("Null name");
        
        this.name = name;
    }
    
    /**
     * Returns a string representation of this {@code CacheType}.
     * 
     * @return the string representation of this {@code CacheType}.
     */
    public String encode()
    {
        return name;
    }
    
    /**
     * Decodes a string into a {@code CacheType}.
     * 
     * @param name string representation of a {@code CacheType}.
     * 
     * @return the corresponding {@code CacheType},
     *         or {@code null} if no {@code CacheType} was found for the given string.
     */
    public static CacheType decode(String name)
    {
        for (CacheType value : values())
        {
            if (value.encode().equals(name))
            {
                return value;
            }
        }
        
        return null;
    }
    
    private final String name;
}
