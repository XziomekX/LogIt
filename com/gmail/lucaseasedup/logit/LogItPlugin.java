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
package com.gmail.lucaseasedup.logit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.PropertyResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The LogIt plugin.
 * 
 * @author LucasEasedUp
 */
public final class LogItPlugin extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        //dataFolder = getDataFolder();
        //
        //if (!dataFolder.exists())
        //    dataFolder.mkdir();
        //
        try
        {
            loadMessages();
        }
        catch (IOException ex)
        {
            log(WARNING, "Could not load messages.");
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
    
    public String getVersion()
    {
        return getDescription().getVersion();
    }
    
    public LogItCore getCore()
    {
        return core;
    }
    
    /**
     * Loads messages from the file.
     * 
     * @throws IOException
     */
    private void loadMessages() throws IOException
    {
        String suffix = "_" + getConfig().getString("locale", "en");
        
        JarFile jarFile = new JarFile(getFile());
        JarEntry jarEntry = jarFile.getJarEntry("messages" + suffix + ".properties");
        
        if (jarEntry == null)
            jarEntry = jarFile.getJarEntry("messages.properties");
        
        if (jarEntry == null)
            throw new FileNotFoundException();
        
        prb = new PropertyResourceBundle(new InputStreamReader(jarFile.getInputStream(jarEntry), "UTF-8"));
    }
    
    /**
     * Returns a message with the secified label.
     * 
     * @param label Message label.
     * @return Message.
     */
    public static String getMessage(String label)
    {
        String message;
        
        try
        {
            message = formatColorCodes(prb.getString(label));
        }
        catch (Exception ex)
        {
            return label;
        }
        
        return message;
    }
    
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
    
    public static boolean isPlayerOnline(String username)
    {
        return (getPlayer(username) != null) ? true : false;
    }
    
    public static Player getPlayer(String username)
    {
        return Bukkit.getServer().getPlayerExact(username);
    }
    
    /**
     * Sends the given message to a player.
     * 
     * @param username Player's username.
     * @param message Message.
     */
    public static void sendMessage(String username, String message)
    {
        Player player = getPlayer(username);
        
        if (player != null)
        {
            player.sendMessage(message);
        }
    }
    
    /**
     * Sends the given message to all online players.
     * 
     * @param message Message.
     */
    public static void broadcastMessage(String message)
    {
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        
        for (Player player : players)
        {
            player.sendMessage(message);
        }
    }
    
    public static void log(Level level, String message)
    {
        Logger.getLogger("Minecraft").log(level, "[LogIt] " + stripColor(message));
        
        if (level.equals(SEVERE))
        {
            Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("LogIt"));
        }
    }
    
    private static PropertyResourceBundle prb;
    
    //private File dataFolder;
    private LogItCore core;
}
