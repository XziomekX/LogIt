/*
 * PlayerUtils.java
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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import java.util.Arrays;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PlayerUtils
{
    private PlayerUtils()
    {
    }
    
    /**
     * Returns a case-correct player name.
     * 
     * @param name the name of a player.
     * 
     * @return the case-correct player name.
     */
    public static String getPlayerName(String name)
    {
        if (isPlayerOnline(name))
        {
            return getPlayer(name).getName();
        }
        else
        {
            return name;
        }
    }
    
    /**
     * Checks if a player with the given name is online.
     * 
     * @param name the player name.
     * 
     * @return {@code true} if online; {@code false} otherwise.
     */
    public static boolean isPlayerOnline(String name)
    {
        return (getPlayer(name) != null) ? true : false;
    }
    
    /**
     * Returns an online {@code Player} with the given name.
     * 
     * @param name the player name.
     * 
     * @return the player object.
     */
    public static Player getPlayer(String name)
    {
        return Bukkit.getPlayerExact(name);
    }
    
    public static String getPlayerIp(Player player)
    {
        if (player.getAddress() == null)
            return "";
        
        return player.getAddress().getAddress().getHostAddress();
    }
    
    /**
     * Checks if the player is within the specified radius of a {@link org.bukkit.Location}.
     * 
     * @param player   a player whose location will be checked.
     * @param location the location which will be compared with the player location.
     * @param radiusX  the maximum radius on X-axis.
     * @param radiusY  the maximum radius on Y-axis.
     * @param radiusZ  the maximum radius on Z-axis.
     * 
     * @return {@code true} if the player is within the radius; {@code false} otherwise.
     */
    public static boolean isPlayerAt(Player player, Location location,
                                     double radiusX, double radiusY, double radiusZ)
    {
        Location playerLocation = player.getLocation();
        
        if (playerLocation.getWorld() != location.getWorld())
            return false;
        
        if (Math.abs(playerLocation.getX() - location.getX()) > radiusX)
            return false;

        if (Math.abs(playerLocation.getY() - location.getY()) > radiusY)
            return false;

        if (Math.abs(playerLocation.getZ() - location.getZ()) > radiusZ)
            return false;
        
        return true;
    }
    
    /**
     * Sends player a message.
     * 
     * @param name    the name of the player who will receive the message.
     * @param message the message to be sent.
     */
    public static void sendMessage(String name, String message)
    {
        Player player = getPlayer(name);
        
        if (player != null)
        {
            player.sendMessage(message);
        }
    }
    
    /**
     * Sends a message to all online players.
     * 
     * @param message the message to be sent.
     */
    public static void broadcastMessage(String message)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            p.sendMessage(message);
        }
    }
    
    /**
     * Sends a message to all online players, except
     * those with names contained in {@code exceptPlayers}.
     * 
     * @param message       the message to be sent.
     * @param exceptPlayers the players to be omitted in broadcasting.
     */
    public static void broadcastMessageExcept(String message, Collection<Player> exceptPlayers)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (!exceptPlayers.contains(p))
            {
                p.sendMessage(message);
            }
        }
    }
    
    /**
     * Broadcasts a join message.
     * 
     * @param player           the player who joined.
     * @param revealSpawnWorld whether to show name of the world
     *                         where the player spawned after joining.
     */
    public static void broadcastJoinMessage(Player player, boolean revealSpawnWorld)
    {
        String joinMessage = JoinMessageGenerator.generate(player, revealSpawnWorld);
        
        broadcastMessageExcept(joinMessage, Arrays.asList(player));
    }
    
    /**
     * Broadcasts a quit message.
     * 
     * @param player the player who quit.
     */
    public static void broadcastQuitMessage(Player player)
    {
        String quitMessage = getMessage("QUIT")
                .replace("%player%", player.getName());
        
        broadcastMessageExcept(quitMessage, Arrays.asList(player));
    }
}
