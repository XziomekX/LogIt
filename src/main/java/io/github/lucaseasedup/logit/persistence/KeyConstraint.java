package io.github.lucaseasedup.logit.persistence;

public enum KeyConstraint
{
    /**
     * All values are considered non-null.
     */
    NONE,
    
    /**
     * Null values are considered null.
     */
    NON_NULL,
    
    /**
     * Null and empty strings are considered null.
     */
    NOT_EMPTY,
    
    /**
     * Null and blank (empty or consisting only of whitespace)
     * strings are considered null.
     */
    NOT_BLANK,
}
