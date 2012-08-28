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
