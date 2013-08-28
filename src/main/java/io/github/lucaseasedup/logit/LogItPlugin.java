/*
 * LogItPlugin.java
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
package io.github.lucaseasedup.logit;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLACK;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.DARK_AQUA;
import static org.bukkit.ChatColor.DARK_BLUE;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.DARK_GREEN;
import static org.bukkit.ChatColor.DARK_PURPLE;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.ChatColor.LIGHT_PURPLE;
import static org.bukkit.ChatColor.MAGIC;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.STRIKETHROUGH;
import static org.bukkit.ChatColor.UNDERLINE;
import static org.bukkit.ChatColor.WHITE;
import static org.bukkit.ChatColor.YELLOW;
import io.github.lucaseasedup.logit.config.Location;
import io.github.lucaseasedup.logit.craftreflect.CraftReflect;
import io.github.lucaseasedup.logit.util.FileUtils;
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
            String version = getCraftBukkitVersion();
            String craftClassName =
                    "io.github.lucaseasedup.logit.craftreflect." + version + ".CraftReflect";
            Class<?> craftClass = Class.forName(craftClassName);
            
            craftReflect = (CraftReflect) craftClass.getConstructor().newInstance();
        }
        catch (ClassNotFoundException ex)
        {
            logger.log(Level.SEVERE, "LogIt does not support this version of Bukkit.");
            disable();
            
            return;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Could not set up CraftBukkit reflection.", ex);
            disable();
            
            return;
        }
        
        try
        {
            // Load messages from the file.
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
            craftReflect = null;
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
        File file;
        
        if ((file = new File(getDataFolder(), "lang/messages" + suffix + ".properties")).exists())
        {
            messages = new PropertyResourceBundle(new FileInputStream(file));
            
            return;
        }
        
        if ((file = new File(getDataFolder(), "lang/messages.properties")).exists())
        {
            messages = new PropertyResourceBundle(new FileInputStream(file));
            
            return;
        }
        
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
    }
    
    public CraftReflect getCraftReflect()
    {
        return craftReflect;
    }
    
    public static String getMessage(String label, String[] variables)
    {
        String message;
        
        try
        {
            message = formatColorCodes(messages.getString(label));
        }
        catch (NullPointerException | MissingResourceException | ClassCastException ex)
        {
            return label;
        }
        
        return parseMessage(message, variables);
    }
    
    public static String getMessage(String label)
    {
        return getMessage(label, NO_STRINGS);
    }
    
    public static String parseMessage(String message, String[] variables)
    {
        for (int i = 0; i < variables.length; i += 2)
        {
            message = message.replace(variables[i], variables[i + 1]);
        }
        
        message = message.replace("%bukkit_version%", Bukkit.getBukkitVersion());
        message = message.replace("%logit_version%",
                LogItPlugin.getInstance().getDescription().getVersion());
        message = message.replace("%server_id%", Bukkit.getServerId());
        message = message.replace("%server_ip%", Bukkit.getIp());
        message = message.replace("%server_motd%", Bukkit.getMotd());
        message = message.replace("%server_name%", Bukkit.getServerName());
        
        return message;
    }
    
    public static String parseMessage(String message)
    {
        return parseMessage(message, NO_STRINGS);
    }
    
    /**
     * Replaces macros with their ChatColor equivalents.
     * 
     * @param s the string to be formatted.
     * @return formatted string.
     */
    public static String formatColorCodes(String s)
    {
        s = s.replace("&0", BLACK.toString());
        s = s.replace("&1", DARK_BLUE.toString());
        s = s.replace("&2", DARK_GREEN.toString());
        s = s.replace("&3", DARK_AQUA.toString());
        s = s.replace("&4", DARK_RED.toString());
        s = s.replace("&5", DARK_PURPLE.toString());
        s = s.replace("&6", GOLD.toString());
        s = s.replace("&7", GRAY.toString());
        s = s.replace("&8", DARK_GRAY.toString());
        s = s.replace("&9", BLUE.toString());
        s = s.replace("&a", GREEN.toString());
        s = s.replace("&b", AQUA.toString());
        s = s.replace("&c", RED.toString());
        s = s.replace("&d", LIGHT_PURPLE.toString());
        s = s.replace("&e", YELLOW.toString());
        s = s.replace("&f", WHITE.toString());
        
        s = s.replace("&l", BOLD.toString());
        s = s.replace("&o", ITALIC.toString());
        s = s.replace("&n", UNDERLINE.toString());
        s = s.replace("&m", STRIKETHROUGH.toString());
        s = s.replace("&k", MAGIC.toString());
        s = s.replace("&r", RESET.toString());
        
        return s;
    }
    
    public static String getCraftBukkitVersion()
    {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] packageParts = packageName.split("\\.");
        
        return packageParts[packageParts.length - 1];
    }
    
    public static void loadLibrary(String filename)
    {
        try
        {
            FileUtils.loadLibrary(filename);
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
        ConfigurationSerialization.registerClass(Location.class);
    }
    
    private static final String[] NO_STRINGS = new String[0];
    
    private static Logger logger;
    private static PropertyResourceBundle messages;
    
    private LogItCore core;
    private CraftReflect craftReflect;
}
