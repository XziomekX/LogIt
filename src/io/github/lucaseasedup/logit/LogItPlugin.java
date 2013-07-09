/*
 * LogItPlugin.java
 *
 * Copyright (C) 2012 LucasEasedUp
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import static java.util.logging.Level.WARNING;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author LucasEasedUp
 */
public final class LogItPlugin extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        try
        {
            // Load messages from the file.
            loadMessages();
        }
        catch (IOException ex)
        {
            // If messages could not be loaded, just log the failure.
            // They're not nessesary for LogIt to work.
            getLogger().log(WARNING, "Could not load messages.");
        }
        
        core = LogItCore.getInstance();
        core.start();
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
     * Returns the LogIt core.
     * 
     * It is not the preferred way to get the core. You should use LogItCore.getInstance() instead,
     * although getCore() works exactly the same.
     * 
     * @return The LogIt core.
     */
    public LogItCore getCore()
    {
        return core;
    }
    
    /**
     * Loads messages from file.
     * <p/>
     * First, it tries to load messages_{locale}.properties (where {locale} is the corresponding value from the config file).
     * If it does not exist, it tries to load messages.properties. If this fails too, it throws a FileNotFoundException.
     * 
     * @throws FileNotFoundException Thrown if no message file has been found.
     * @throws IOException Thrown if there was an error while reading.
     */
    private void loadMessages() throws IOException
    {
        String suffix = "_" + getConfig().getString("locale", "en");
        
        try (JarFile jarFile = new JarFile(getFile()))
        {
            JarEntry jarEntry = jarFile.getJarEntry("messages" + suffix + ".properties");
            
            if (jarEntry == null)
                jarEntry = jarFile.getJarEntry("messages.properties");
            
            if (jarEntry == null)
                throw new FileNotFoundException("No message files found.");
            
            prb = new PropertyResourceBundle(new InputStreamReader(jarFile.getInputStream(jarEntry), "UTF-8"));
        }
    }
    
    /**
     * Returns a message with the secified label.
     * 
     * @param label Message label.
     * @return Message with that label.
     */
    public static String getMessage(String label)
    {
        String message;
        
        try
        {
            message = formatColorCodes(prb.getString(label));
        }
        catch (NullPointerException | MissingResourceException | ClassCastException ex)
        {
            return label;
        }
        
        return message;
    }
    
    /**
     * Replaces macros with their ChatColor equivalents.
     * 
     * @param s String to be formatted.
     * @return Formatted string.
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
    
    /**
     * Provides a shortcut to Bukkit.getPluginManager().callEvent().
     * 
     * @param event Event to be called.
     */
    public static void callEvent(Event event)
    {
        Bukkit.getPluginManager().callEvent(event);
    }
    
    private static PropertyResourceBundle prb;
    
    private LogItCore core;
}
