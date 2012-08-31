/*
 * MessageSender.java
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
package com.gmail.lucaseasedup.logit.util;

import com.gmail.lucaseasedup.logit.AccountManager;
import com.gmail.lucaseasedup.logit.LogItConfiguration;
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class MessageSender
{
    private MessageSender()
    {
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
        for (Player p : Bukkit.getOnlinePlayers())
        {
            p.sendMessage(message);
        }
    }
    
    /**
     * Sends the given message to all online players, except for a player specified as a first parameter.
     * 
     * @param message Message.
     * @param player Player to be omitted in broadcasting.
     */
    public static void broadcastMessage(String message, Player player)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (!p.equals(player))
            {
                p.sendMessage(message);
            }
        }
    }
    
    /**
     * Broadcasts a join message.
     * 
     * @param player Player who joined.
     */
    public static void broadcastJoinMessage(Player player, LogItConfiguration config)
    {
        String message = getMessage("JOIN");
        
        message = message.replace("%player%", player.getName());
        message = message.replace("%in_world%", (config.isShowSpawnWorldInfoEnabled()) ? SpawnWorldInfoGenerator.generate(player) : "");
        
        broadcastMessage(message, player);
    }
    
    /**
     * Broadcasts a quit message.
     * 
     * @param player Player who quit.
     */
    public static void broadcastQuitMessage(Player player)
    {
        String message = getMessage("QUIT");
        
        message = message.replace("%player%", player.getName());
        
        broadcastMessage(message, player);
    }
    
    /**
     * Sends a message to the specified player telling them to either login or register.
     * 
     * @param player Player.
     */
    public static void sendForceLoginMessage(Player player, AccountManager accountManager)
    {
        if (accountManager.isAccountCreated(player.getName()))
        {
            player.sendMessage(getMessage("PLEASE_LOGIN"));
        }
        else
        {
            player.sendMessage(getMessage("PLEASE_REGISTER"));
        }
    }
    
    /**
     * Returns a case-correct username.
     * 
     * @param username Username.
     * @return Case-correct username.
     */
    public static String getPlayerName(String username)
    {
        if (isPlayerOnline(username))
        {
            return getPlayer(username).getName();
        }
        else
        {
            return username;
        }
    }
    
    /**
     * Checks if a player with the given username is online.
     * 
     * @param username Username.
     * @return True, if they are online.
     */
    public static boolean isPlayerOnline(String username)
    {
        return (getPlayer(username) != null) ? true : false;
    }
    
    /**
     * Returns a Player instance with the given username.
     * 
     * @param username Username.
     * @return Player.
     */
    public static Player getPlayer(String username)
    {
        return Bukkit.getPlayerExact(username);
    }
}
