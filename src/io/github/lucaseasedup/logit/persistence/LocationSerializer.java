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

import io.github.lucaseasedup.logit.LogItCore;
import java.util.Map;
import org.bukkit.Bukkit;
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
@EnabledConfigProperty("waiting-room.enabled")
public final class LocationSerializer extends PersistenceSerializer
{
    public LocationSerializer(LogItCore core)
    {
        super(core);
    }
    
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
            player.teleport(getWaitingRoomLocation());
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
