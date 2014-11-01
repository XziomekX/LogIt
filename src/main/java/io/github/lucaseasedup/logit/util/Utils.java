package io.github.lucaseasedup.logit.util;

import io.github.lucaseasedup.logit.LogItCore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

public final class Utils
{
    private Utils()
    {
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
}
