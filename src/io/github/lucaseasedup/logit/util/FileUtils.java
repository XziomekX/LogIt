/*
 * FileUtils.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import org.bukkit.Bukkit;

/**
 * @author LucasEasedUp
 */
public class FileUtils
{
    private FileUtils()
    {
    }
    
    public static void extractResource(String resource, File dest) throws IOException
    {
        int readBytes;
        byte[] buffer = new byte[4096];
        
        try (
            InputStream is = FileUtils.class.getResourceAsStream(resource);
            OutputStream os = new FileOutputStream(dest);
        )
        {
            while ((readBytes = is.read(buffer)) > 0)
            {
                os.write(buffer, 0, readBytes);
            }
        }
    }
    
    public static void downloadFile(String url, File dest) throws IOException
    {
        ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());

        try (FileOutputStream fos = new FileOutputStream(dest))
        {
            fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
        }
    }
    
    public static void downloadLibrary(String url, String filename) throws IOException
    {
        downloadFile(url, new File(Bukkit.getPluginManager().getPlugin("LogIt").getDataFolder(), "lib/" + filename));
    }
    
    public static boolean libraryDownloaded(String filename)
    {
        return new File(Bukkit.getPluginManager().getPlugin("LogIt").getDataFolder(), "lib/" + filename).exists();
    }
    
    public static void loadLibrary(String filename) throws ReflectiveOperationException, MalformedURLException
    {
        File file = new File(Bukkit.getPluginManager().getPlugin("LogIt").getDataFolder(), "lib/" + filename);
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        
        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        addUrlMethod.setAccessible(true);
        addUrlMethod.invoke(classLoader, new Object[]{file.toURI().toURL()});
    }
    
    public static boolean libraryLoaded(String filename)
    {
        File file = new File(Bukkit.getPluginManager().getPlugin("LogIt").getDataFolder(), "lib/" + filename);
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        
        try
        {
            return Arrays.asList(classLoader.getURLs()).contains(file.toURI().toURL());
        }
        catch (MalformedURLException ex)
        {
            return false;
        }
    }
}
