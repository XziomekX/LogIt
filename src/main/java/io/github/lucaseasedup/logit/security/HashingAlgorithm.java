package io.github.lucaseasedup.logit.security;

public enum HashingAlgorithm
{
    PLAIN("plain"),
    MD2("md2"), MD5("md5"),
    SHA1("sha-1"), SHA256("sha-256"), SHA384("sha-384"), SHA512("sha-512"),
    WHIRLPOOL("whirlpool"), BCRYPT("bcrypt"), AUTHME("authme");
    
    private HashingAlgorithm(String name)
    {
        if (name == null)
            throw new IllegalArgumentException("Null name");
        
        this.name = name;
    }
    
    /**
     * Returns a string representation of this {@code HashingAlgorithm}.
     * 
     * @return the string representation of this {@code HashingAlgorithm}.
     */
    public String encode()
    {
        return name;
    }
    
    /**
     * Decodes a string into a {@code HashingAlgorithm}.
     * 
     * @param name string representation of a {@code HashingAlgorithm}.
     * 
     * @return the corresponding {@code HashingAlgorithm},
     *         or {@code null} if no {@code HashingAlgorithm} was found for the given string.
     */
    public static HashingAlgorithm decode(String name)
    {
        for (HashingAlgorithm value : values())
        {
            if (value == AUTHME && name.startsWith("authme:"))
            {
                return value;
            }
            
            if (value.encode().equals(name))
            {
                return value;
            }
        }
        
        return null;
    }
    
    private final String name;
}
