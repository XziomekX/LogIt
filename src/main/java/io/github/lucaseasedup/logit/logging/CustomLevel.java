package io.github.lucaseasedup.logit.logging;

import java.util.logging.Level;

public final class CustomLevel extends Level
{
    public CustomLevel(String name, int value)
    {
        super(name, value);
    }
    
    /**
     * INTERNAL is a message level providing internal information
     * typically used for debugging.
     */
    public static final Level INTERNAL = new CustomLevel("INTERNAL", -1000);
    
    private static final long serialVersionUID = 1L;
}
