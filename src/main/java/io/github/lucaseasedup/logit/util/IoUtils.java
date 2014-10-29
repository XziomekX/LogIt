package io.github.lucaseasedup.logit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class IoUtils
{
    private IoUtils()
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
                    ByteArrayInputStream bain = new ByteArrayInputStream(baos.toByteArray());
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
    
    public static String toString(InputStream is) throws IOException
    {
        StringWriter sw = new StringWriter();
        InputStreamReader isr = new InputStreamReader(is);
        
        char[] buffer = new char[1024 * 4];
        int n = 0;
        
        while (-1 != (n = isr.read(buffer)))
        {
            sw.write(buffer, 0, n);
        }
        
        return sw.toString();
    }
    
    public static String toString(File file) throws IOException
    {
        try (InputStream is = new FileInputStream(file))
        {
            return IoUtils.toString(is);
        }
    }
    
    public static void extractResource(String resource, File dest) throws IOException
    {
        int readBytes;
        byte[] buffer = new byte[4096];
        
        String jarUrlPath =
                IoUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String jarPath = URLDecoder.decode(jarUrlPath, "UTF-8");
        
        try (
            ZipFile jarZipFile = new ZipFile(jarPath);
            OutputStream os = new FileOutputStream(dest);
        )
        {
            ZipEntry entry = jarZipFile.getEntry(resource);
            
            if (entry != null)
            {
                try (InputStream is = jarZipFile.getInputStream(entry))
                {
                    while ((readBytes = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, readBytes);
                    }
                }
            }
        }
    }
    
    public static void copyFile(File sourceFile, File destFile)
            throws IOException
    {
        if (!destFile.exists())
        {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
        }
        
        try(
            FileInputStream source = new FileInputStream(sourceFile);
            FileOutputStream destination = new FileOutputStream(destFile);
        )
        {
            try (
                FileChannel sourceChannel = source.getChannel();
                FileChannel destinationChannel = destination.getChannel();
            )
            {
                destinationChannel.transferFrom(
                        sourceChannel, 0, sourceChannel.size()
                );
            }
        }
    }
}
