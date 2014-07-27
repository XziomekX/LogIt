/*
 * LocationSerializer.java
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
package io.github.lucaseasedup.logit.persistence;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Keys({
    @Key(name = "world", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "x", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "y", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "z", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "yaw", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "pitch", constraint = KeyConstraint.NOT_EMPTY),
})
public final class LocationSerializer extends LogItCoreObject implements PersistenceSerializer
{
    @Override
    public void serialize(Map<String, String> data, final Player player)
    {
        serialize(data, player.getLocation());
        
        if (player.isOnline())
        {
            final Location waitingRoomLocation = getWaitingRoomLocation();
            
            player.teleport(waitingRoomLocation);
            
            final int teleportPasses = getConfig("secret.yml")
                    .getInt("locationSerializer.teleportCheck.passes");
            final double dislocationRadius = getConfig("secret.yml")
                    .getDouble("locationSerializer.teleportCheck.dislocationRadius");
            
            // Make sure that no other plugin teleported the player away.
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (++passes > teleportPasses)
                    {
                        cancel();
                    }
                    else if (!PlayerUtils.isPlayerAt(player, waitingRoomLocation,
                            dislocationRadius, dislocationRadius, dislocationRadius))
                    {
                        player.teleport(waitingRoomLocation);
                    }
                }
                
                /**
                 * Tells how many checks has been done so far.
                 */
                private int passes = 0;
            }.runTaskTimer(getPlugin(), 1L, 1L);
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, Player player)
    {
        if (player.isOnline())
        {
            player.teleport(unserialize(data));
        }
    }
    
    private org.bukkit.Location getWaitingRoomLocation()
    {
        return getConfig("config.yml").getLocation("waitingRoom.location").toBukkitLocation();
    }
    
    private static void serialize(Map<String, String> data, org.bukkit.Location location)
    {
        data.put("world", location.getWorld().getName());
        data.put("x", String.valueOf(location.getX()));
        data.put("y", String.valueOf(location.getY()));
        data.put("z", String.valueOf(location.getZ()));
        data.put("yaw", String.valueOf(location.getYaw()));
        data.put("pitch", String.valueOf(location.getPitch()));
    }
    
    private static org.bukkit.Location unserialize(Map<String, String> data)
    {
        return new org.bukkit.Location(
                Bukkit.getWorld(data.get("world")),
                Double.valueOf(data.get("x")),
                Double.valueOf(data.get("y")),
                Double.valueOf(data.get("z")),
                Float.valueOf(data.get("yaw")),
                Float.valueOf(data.get("pitch"))
        );
    }
    
    @Keys({
        @Key(name = "world", constraint = KeyConstraint.NOT_EMPTY),
        @Key(name = "x", constraint = KeyConstraint.NOT_EMPTY),
        @Key(name = "y", constraint = KeyConstraint.NOT_EMPTY),
        @Key(name = "z", constraint = KeyConstraint.NOT_EMPTY),
        @Key(name = "yaw", constraint = KeyConstraint.NOT_EMPTY),
        @Key(name = "pitch", constraint = KeyConstraint.NOT_EMPTY),
    })
    public static final class PlayerlessLocationSerializer implements PersistenceSerializer
    {
        public PlayerlessLocationSerializer(org.bukkit.Location location)
        {
            if (location == null)
                throw new IllegalArgumentException();
            
            this.location = location;
        }
        
        @Override
        public void serialize(Map<String, String> data, Player player)
        {
            LocationSerializer.serialize(data, location);
        }
        
        @Override
        public void unserialize(Map<String, String> data, Player player)
        {
            location = LocationSerializer.unserialize(data);
        }
        
        public Location getLocation()
        {
            return location;
        }
        
        private Location location;
    }
}
