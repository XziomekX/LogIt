package io.github.lucaseasedup.logit.util;

import io.github.lucaseasedup.logit.LogItCore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class Utils
{
    private Utils()
    {
    }
    
    public static String[] getWords(String string)
    {
        String trim = string.trim();
        
        if (trim.isEmpty())
            return ArrayUtils.NO_STRINGS;
        
        return WORD_SPACE_PATTERN.split(trim);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj)
    {
        if (obj == null)
            throw new IllegalArgumentException();
        
        try
        {
            try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
            )
            {
                oos.writeObject(obj);
                oos.flush();
                
                try (
                    ByteArrayInputStream bain =
                            new ByteArrayInputStream(baos.toByteArray());
                    ObjectInputStream ois = new ObjectInputStream(bain);
                )
                {
                    return (T) ois.readObject();
                }
            }
        }
        catch (IOException | ClassNotFoundException ex)
        {
            LogItCore.getInstance().log(Level.WARNING, ex);
            
            return null;
        }
    }
    
    private static final Pattern WORD_SPACE_PATTERN =
            Pattern.compile("\\s+");
}
