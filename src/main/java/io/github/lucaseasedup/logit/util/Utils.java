package io.github.lucaseasedup.logit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class Utils
{
    private Utils()
    {
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj)
    {
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
            return null;
        }
    }
}
