package io.github.lucaseasedup.logit.storage;

public enum StorageType
{
    UNKNOWN, NONE, SQLITE, MYSQL, H2, POSTGRESQL, CSV;
    
    public static StorageType decode(String s)
    {
        switch (s.toLowerCase())
        {
        case "none":       return NONE;
        case "sqlite":     return SQLITE;
        case "mysql":      return MYSQL;
        case "h2":         return H2;
        case "postgresql": return POSTGRESQL;
        case "csv":        return CSV;
        default:           return UNKNOWN;
        }
    }
    
    /**
     * Converts this {@code StorageType} to a string representation.
     * 
     * @return the string representation of this {@code StorageType},
     *         or {@code null} if no representation for this
     *         {@code StorageType} was implemented.
     */
    public String encode()
    {
        switch (this)
        {
        case NONE:       return "none";
        case SQLITE:     return "sqlite";
        case MYSQL:      return "mysql";
        case H2:         return "h2";
        case POSTGRESQL: return "postgresql";
        case CSV:        return "csv";
        default:         return null;
        }
    }
}
