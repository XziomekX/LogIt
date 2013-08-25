/*
 * LocationSerializer.java
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
package io.github.lucaseasedup.logit.persistence;

import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
@Keys({
    @Key(name = "world", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "x", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "y", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "z", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "yaw", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "pitch", constraint = KeyConstraint.NOT_EMPTY),
})
public final class LocationSerializer extends PersistenceSerializer
{
    @Override
    public void serialize(Map<String, String> data, final Player player)
    {
        org.bukkit.Location location = player.getLocation();
        
        data.put("world", location.getWorld().getName());
        data.put("x", String.valueOf(location.getX()));
        data.put("y", String.valueOf(location.getY()));
        data.put("z", String.valueOf(location.getZ()));
        data.put("yaw", String.valueOf(location.getYaw()));
        data.put("pitch", String.valueOf(location.getPitch()));
        
        if (player.isOnline())
        {
            final Location waitingRoomLocation = getWaitingRoomLocation();
            final AtomicInteger teleporterTaskId = new AtomicInteger();
            
            // First teleport attempt is not delayed.
            player.teleport(waitingRoomLocation);
            
            // Now, check if player location changed (e.g. by another plugin).  
            teleporterTaskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(),
                    new Runnable()
            {
                @Override
                public void run()
                {
                    if (++passes > MAX_PASSES)
                    {
                        Bukkit.getScheduler().cancelTask(teleporterTaskId.get());
                    }
                    else if (!PlayerUtils.isPlayerAt(player, waitingRoomLocation, 0.5, 0.5, 0.5))
                    {
                        player.teleport(waitingRoomLocation);
                    }
                }
                
                /**
                 * Tells how many times the location will be checked.
                 */
                private final int MAX_PASSES = 2;
                
                /**
                 * Tells how many checks has been done so far.
                 */
                private int passes = 0;
            }, 1L, 1L));
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, Player player)
    {
        if (player.isOnline())
        {
            player.teleport(new org.bukkit.Location(
                Bukkit.getWorld(data.get("world")),
                Double.valueOf(data.get("x")),
                Double.valueOf(data.get("y")),
                Double.valueOf(data.get("z")),
                Float.valueOf(data.get("yaw")),
                Float.valueOf(data.get("pitch"))
            ));
        }
    }
    
    private org.bukkit.Location getWaitingRoomLocation()
    {
        return getConfig().getLocation("waiting-room.location").toBukkitLocation();
    }
}
