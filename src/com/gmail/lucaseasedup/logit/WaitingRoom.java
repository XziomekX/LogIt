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
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class WaitingRoom
{
    public WaitingRoom()
    {
    }
    
    public void put(Player player)
    {
        // Back up player's location.
        locations.put(player, player.getLocation().clone());
        
        // Put the player into the waiting room.
        player.teleport(LogItCore.getInstance().getConfig().getWaitingRoomLocation());
    }
    
    public void remove(Player player)
    {
        if (!contains(player))
            return;
        
        // Take the player out of waiting room.
        player.teleport(locations.remove(player));
    }
    
    public boolean contains(Player player)
    {
        return locations.containsKey(player);
    }
    
    private final HashMap<Player, Location> locations = new HashMap<>();
}
