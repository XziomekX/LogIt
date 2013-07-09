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
package io.github.lucaseasedup.logit.util;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.account.AccountManager;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
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
     * @param revealSpawnWorld Whether the spawn-world should be shown along with the join message.
     */
    public static void broadcastJoinMessage(Player player, boolean revealSpawnWorld)
    {
        String joinMessage = JoinMessageGenerator.generate(player, revealSpawnWorld);
        
        broadcastMessage(joinMessage, player);
    }
    
    /**
     * Broadcasts a quit message.
     * 
     * @param player Player who quit.
     */
    public static void broadcastQuitMessage(Player player)
    {
        String quitMessage = getMessage("QUIT")
                .replace("%player%", player.getName());
        
        broadcastMessage(quitMessage, player);
    }
    
    /**
     * Sends a message to the specified player telling them to either login or register.
     * 
     * @param player Player.
     * @param accountManager AccountManager.
     */
    public static void sendForceLoginMessage(Player player, AccountManager accountManager)
    {
        if (accountManager.isRegistered(player.getName()))
        {
            player.sendMessage(getMessage("PLEASE_LOGIN"));
        }
        else
        {
            player.sendMessage(getMessage("PLEASE_REGISTER"));
        }
    }
}
