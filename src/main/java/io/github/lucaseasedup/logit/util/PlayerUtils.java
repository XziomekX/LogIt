/*
 * PlayerUtils.java
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public final class PlayerUtils
{
    private PlayerUtils()
    {
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
     * Returns a player with the given username.
     * 
     * @param username Username.
     * @return Player.
     */
    public static Player getPlayer(String username)
    {
        return Bukkit.getPlayerExact(username);
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
}
