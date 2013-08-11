/*
 * WaitingRoom.java
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
package io.github.lucaseasedup.logit;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author LucasEasedUp
 */
public final class WaitingRoom extends LogItCoreObject
{
    public WaitingRoom(LogItCore core)
    {
        super(core);
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
        
        if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.persistence"))
        {
            if (getAccountManager().isRegistered(player.getName()))
            {
                Location loc = player.getLocation();
                
                getAccountManager().updatePersistence(player.getName(), "world", loc.getWorld().getName());
                getAccountManager().updatePersistence(player.getName(), "x", String.valueOf(loc.getX()));
                getAccountManager().updatePersistence(player.getName(), "y", String.valueOf(loc.getY()));
                getAccountManager().updatePersistence(player.getName(), "z", String.valueOf(loc.getZ()));
                getAccountManager().updatePersistence(player.getName(), "yaw", String.valueOf(loc.getYaw()));
                getAccountManager().updatePersistence(player.getName(), "pitch", String.valueOf(loc.getPitch()));
                getAccountManager().updatePersistence(player.getName(), "waiting_room", "1");
            }
            
            player.teleport(getWaitingRoomLocation());
        }
        else if (!getAccountManager().isRegistered(player.getName()))
        {
            player.teleport(getWaitingRoomLocation());
        }
        
        players.add(player);
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
        if (!contains(player) || !getCore().isPlayerForcedToLogin(player))
            return;
        
        if (getAccountManager().isRegistered(player.getName())
                && !getAccountManager().getTable().isColumnDisabled("logit.accounts.persistence"))
        {
            player.teleport(new Location(
                Bukkit.getWorld(getAccountManager().getPersistence(player.getName(), "world")),
                Double.valueOf(getAccountManager().getPersistence(player.getName(), "x")),
                Double.valueOf(getAccountManager().getPersistence(player.getName(), "y")),
                Double.valueOf(getAccountManager().getPersistence(player.getName(), "z")),
                Float.valueOf(getAccountManager().getPersistence(player.getName(), "yaw")),
                Float.valueOf(getAccountManager().getPersistence(player.getName(), "pitch"))
            ));
            
            getAccountManager().updatePersistence(player.getName(), "waiting_room", "0");
        }
        
        players.remove(player);
    }
    
    public void removeAll()
    {
        for (Player player : players)
        {
            remove(player);
        }
    }
    
    /**
     * Check if a player is in the waiting room.
     * 
     * @param player Player.
     * @return True if the player is in the waiting room.
     */
    public boolean contains(Player player)
    {
        if (getAccountManager().isRegistered(player.getName()))
        {
            return "1".equals(getAccountManager().getPersistence(player.getName(), "waiting_room"));
        }
        else
        {
            return players.contains(player);
        }
    }
    
    public Location getWaitingRoomLocation()
    {
        World  world = Bukkit.getServer().getWorld(getConfig().getString("waiting-room.location.world"));
        double x = getConfig().getVector("waiting-room.location.position").getX();
        double y = getConfig().getVector("waiting-room.location.position").getY();
        double z = getConfig().getVector("waiting-room.location.position").getZ();
        float  yaw = (float) getConfig().getDouble("waiting-room.location.yaw");
        float  pitch = (float) getConfig().getDouble("waiting-room.location.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public void setWaitingRoomLocation(Location location)
    {
        getConfig().set("waiting-room.location.world", location.getWorld().getName());
        getConfig().set("waiting-room.location.position",
                new Vector(location.getX(), location.getY(), location.getZ()));
        getConfig().set("waiting-room.location.yaw", (double) location.getYaw());
        getConfig().set("waiting-room.location.pitch", (double) location.getPitch());
    }
    
    public Location getNewbieTeleportLocation()
    {
        String worldName = getConfig().getString("waiting-room.newbie-teleport.location.world");
        World  world = Bukkit.getServer().getWorld(worldName);
        double x = getConfig().getVector("waiting-room.newbie-teleport.location.position").getX();
        double y = getConfig().getVector("waiting-room.newbie-teleport.location.position").getY();
        double z = getConfig().getVector("waiting-room.newbie-teleport.location.position").getZ();
        float  yaw = (float) getConfig().getDouble("waiting-room.newbie-teleport.location.yaw");
        float  pitch = (float) getConfig().getDouble("waiting-room.newbie-teleport.location.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    private final Set<Player> players = new HashSet<>();
}
