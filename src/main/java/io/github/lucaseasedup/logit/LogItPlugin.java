/*
 * LogItPlugin.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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
package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.config.LocationSerializable;
import io.github.lucaseasedup.logit.util.IoUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public final class LogItPlugin extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        logger = getLogger();
        
        try
        {
            loadMessages();
        }
        catch (IOException ex)
        {
            // If messages could not be loaded, just log the failure.
            // They're not nessesary for LogIt to work.
            getLogger().log(Level.WARNING, "Could not load messages.", ex);
        }
        
        core = LogItCore.getInstance();
        
        try
        {
            core.start();
        }
        catch (FatalReportedException ex)
        {
            core = null;
            messages = null;
            
            disable();
        }
    }
    
    @Override
    public void onDisable()
    {
        if (core != null)
        {
            core.stop();
        }
    }
    
    public void enable()
    {
        getServer().getPluginManager().enablePlugin(this);
    }
    
    public void disable()
    {
        getServer().getPluginManager().disablePlugin(this);
    }
    
    /**
     * Loads messages from file.
     * 
     * <p> First, it tries to load messages_{locale}.properties from the data folder
     * (where {locale} is the "locale" config property). If it does not exist,
     * it tries to load messages.properties. If this fails too, it does it all again
     * but within JAR file. If the JAR file does not contain any of the aforementioned files,
     * it throws FileNotFoundException.
     * 
     * @throws FileNotFoundException if no message file has been found.
     * @throws IOException if there was an error while reading.
     */
    public void loadMessages() throws IOException
    {
        String suffix = "_" + getConfig().getString("locale", "en");
        
        try (JarFile jarFile = new JarFile(getFile()))
        {
            JarEntry jarEntry = jarFile.getJarEntry("messages" + suffix + ".properties");
            
            if (jarEntry == null)
            {
                jarEntry = jarFile.getJarEntry("messages.properties");
            }
            
            if (jarEntry == null)
                throw new FileNotFoundException("No message files found.");
            
            InputStream messagesInputStream = jarFile.getInputStream(jarEntry);
            
            try (Reader messagesReader = new InputStreamReader(messagesInputStream, "UTF-8"))
            {
                messages = new PropertyResourceBundle(messagesReader);
            }
        }
        
        loadCustomGlobalMessages("lang/messages.properties");
        loadCustomLocalMessages("lang/messages" + suffix + ".properties");
    }
    
    public void loadCustomGlobalMessages(String path) throws IOException
    {
        File file = new File(getDataFolder(), path);
        
        if (!file.exists())
            return;
        
        try (InputStream is = new FileInputStream(file))
        {
            customGlobalMessages = new PropertyResourceBundle(is);
        }
    }
    
    public void loadCustomLocalMessages(String path) throws IOException
    {
        File file = new File(getDataFolder(), path);
        
        if (!file.exists())
            return;
        
        try (InputStream is = new FileInputStream(file))
        {
            customLocalMessages = new PropertyResourceBundle(is);
        }
    }
    
    public static String getMessage(String label)
    {
        String message;
        
        try
        {
            String string = messages.getString(label);
            
            if (customGlobalMessages != null && customGlobalMessages.containsKey(label))
            {
                string = customGlobalMessages.getString(label);
            }
            
            if (customLocalMessages != null && customLocalMessages.containsKey(label))
            {
                string = customLocalMessages.getString(label);
            }
            
            message = ChatColor.translateAlternateColorCodes('&', string);
        }
        catch (NullPointerException | MissingResourceException | ClassCastException ex)
        {
            return label;
        }
        
        return parseMessage(message);
    }
    
    public static String parseMessage(String message)
    {
        message = message.replace("%bukkit_version%", Bukkit.getBukkitVersion());
        message = message.replace("%logit_version%",
                LogItPlugin.getInstance().getDescription().getVersion());
        message = message.replace("%server_id%", Bukkit.getServerId());
        message = message.replace("%server_ip%", Bukkit.getIp());
        message = message.replace("%server_motd%", Bukkit.getMotd());
        message = message.replace("%server_name%", Bukkit.getServerName());
        
        return message;
    }
    
    public static void loadLibrary(String filename)
    {
        try
        {
            IoUtils.loadLibrary(filename);
        }
        catch (FileNotFoundException | MalformedURLException ex)
        {
            logger.log(Level.SEVERE, "Library {0} was not found.", filename);
            getInstance().disable();
            
            ReportedException.throwNew(ex);
        }
        catch (ReflectiveOperationException ex)
        {
            logger.log(Level.SEVERE, "Could not load library " + filename + ".", ex);
            getInstance().disable();
            
            ReportedException.throwNew(ex);
        }
    }
    
    public static LogItPlugin getInstance()
    {
        return (LogItPlugin) Bukkit.getPluginManager().getPlugin("LogIt");
    }
    
    public static Logger getInstanceLogger()
    {
        return logger;
    }
    
    static
    {
        ConfigurationSerialization.registerClass(LocationSerializable.class);
    }
    
    private static Logger logger;
    private static PropertyResourceBundle messages;
    private static PropertyResourceBundle customGlobalMessages;
    private static PropertyResourceBundle customLocalMessages;
    
    private LogItCore core;
}
