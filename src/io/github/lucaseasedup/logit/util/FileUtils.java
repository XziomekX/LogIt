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

import io.github.lucaseasedup.logit.LogItPlugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

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
    
    public static void loadLibrary(String filename) throws ReflectiveOperationException, FileNotFoundException, MalformedURLException
    {
        File file = new File(LogItPlugin.getInstance().getDataFolder(), "lib/" + filename);
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL url = file.toURI().toURL();
        
        if (!Arrays.asList(classLoader.getURLs()).contains(url))
        {
            Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            addUrlMethod.setAccessible(true);
            addUrlMethod.invoke(classLoader, new Object[]{url});
        }
    }
    
    public static boolean libraryLoaded(String filename)
    {
        File file = new File(LogItPlugin.getInstance().getDataFolder(), "lib/" + filename);
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL url;
        
        try
        {
            url = file.toURI().toURL();
        }
        catch (MalformedURLException ex)
        {
            return false;
        }
        
        return Arrays.asList(classLoader.getURLs()).contains(url);
    }
}
