package io.github.lucaseasedup.logit.storage;

public enum DataType
{
    /**
     * Integer-number value.
     */
    INTEGER,
    
    /**
     * Real-number value.
     */
    REAL,
    
    /**
     * Text of maximum length of 255 characters.
     */
    TINYTEXT,
    
    /**
     * Text of maximum length of 1023 characters.
     */
    MEDIUMTEXT,
    
    /**
     * Text of maximum length of 10119 characters.
     */
    LONGTEXT,
    
    /**
     * Text of unlimited length.
     */
    TEXT;
}
