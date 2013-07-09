/*
 * WaitingRoom.java
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

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class WaitingRoom
{
    public WaitingRoom(LogItCore core)
    {
        this.core = core;
    }
    
    /**
     * Puts a player into the waiting room.
     * 
     * If the player is already in the waiting room, no action will be taken.
     * 
     * @param player Player.
     */
    public void put(Player player)
    {
        if (contains(player))
            return;
        
        // Back up player's location.
        locations.put(player, player.getLocation().clone());
        
        // Put the player into the waiting room.
        player.teleport(getLocation());
    }
    
    /**
     * Takes a player out of waiting room.
     * 
     * If the player is not in the waiting room, no action will be taken.
     * 
     * @param player Player.
     */
    public void remove(Player player)
    {
        if (!contains(player))
            return;
        
        // Take the player out of waiting room.
        player.teleport(locations.remove(player));
    }
    
    /**
     * Check if a player is in the waiting room.
     * 
     * @param player Player.
     * @return True if the player is in the waiting room.
     */
    public boolean contains(Player player)
    {
        return locations.containsKey(player);
    }
    
    public Location getLocation()
    {
        World  world = Bukkit.getServer().getWorld(core.getConfig().getString("waiting-room.location.world"));
        double x = core.getConfig().getDouble("waiting-room.location.x");
        double y = core.getConfig().getDouble("waiting-room.location.y");
        double z = core.getConfig().getDouble("waiting-room.location.z");
        float  yaw = (float) core.getConfig().getDouble("waiting-room.location.yaw");
        float  pitch = (float) core.getConfig().getDouble("waiting-room.location.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public void setLocation(Location location)
    {
        core.getConfig().set("waiting-room.location.world", location.getWorld().getName());
        core.getConfig().set("waiting-room.location.x", location.getX());
        core.getConfig().set("waiting-room.location.y", location.getY());
        core.getConfig().set("waiting-room.location.z", location.getZ());
        core.getConfig().set("waiting-room.location.yaw", location.getYaw());
        core.getConfig().set("waiting-room.location.pitch", location.getPitch());
    }
    
    private final LogItCore core;
    
    private final HashMap<Player, Location> locations = new HashMap<>();
}
