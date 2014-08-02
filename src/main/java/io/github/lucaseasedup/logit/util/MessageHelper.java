/*
 * MessageHelper.java
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
package io.github.lucaseasedup.logit.util;

import io.github.lucaseasedup.logit.LogItPlugin;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageHelper
{
    private MessageHelper()
    {
    }
    
    /**
     * Returns the translated message associated with the given label.
     *  
     * @param label the message label.
     * 
     * @return the translated message.
     */
    public static String t(String label)
    {
        return LogItPlugin.getMessage(label);
    }
    
    /**
     * Sends a message to the specified {@code CommandSender}.
     * 
     * <p> If the provided {@code CommandSender} is not a {@code Player},
     * the message will be stripped of colours.
     * 
     * @param sender  the {@code CommandSender} who will receive the message.
     * @param message the message to be sent.
     */
    public static void sendMsg(CommandSender sender, String message)
    {
        if (sender instanceof Player)
        {
            sender.sendMessage(message);
        }
        else
        {
            sender.sendMessage(ChatColor.stripColor(message));
        }
    }
    
    /**
     * Sends a message to a player with the specified name if they are online.
     * 
     * @param playerName a name of the player who will receive the message.
     * @param message    the message to be sent.
     */
    public static void sendMsg(String playerName, String message)
    {
        Player player = Bukkit.getPlayerExact(playerName);
        
        if (player != null)
        {
            sendMsg(player, message);
        }
    }
    
    /**
     * Sends a message to all online players.
     * 
     * @param message the message to be sent.
     */
    public static void broadcastMsg(String message)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            sendMsg(p, message);
        }
    }
    
    /**
     * Sends a message to all online players with an exception to player names
     * confined in {@code exceptPlayers}.
     * 
     * @param message       the message to be broadcasted.
     * @param exceptPlayers the case-insensitive player names {@code Collection}
     *                      that will omitted in the broadcasting.
     */
    public static void broadcastMsgExcept(String message, Collection<String> exceptPlayers)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (!CollectionUtils.containsIgnoreCase(p.getName(), exceptPlayers))
            {
                sendMsg(p, message);
            }
        }
    }
}
