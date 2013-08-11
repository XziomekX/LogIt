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

import io.github.lucaseasedup.logit.config.Location;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
                String username = player.getName();
                org.bukkit.Location loc = player.getLocation();
                
                getAccountManager().updateAccountPersistence(username, "world", loc.getWorld().getName());
                getAccountManager().updateAccountPersistence(username, "x", String.valueOf(loc.getX()));
                getAccountManager().updateAccountPersistence(username, "y", String.valueOf(loc.getY()));
                getAccountManager().updateAccountPersistence(username, "z", String.valueOf(loc.getZ()));
                getAccountManager().updateAccountPersistence(username, "yaw", String.valueOf(loc.getYaw()));
                getAccountManager().updateAccountPersistence(username, "pitch", String.valueOf(loc.getPitch()));
                getAccountManager().updateAccountPersistence(username, "waiting_room", "1");
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
            player.teleport(new org.bukkit.Location(
                Bukkit.getWorld(getAccountManager().getAccountPersistence(player.getName(), "world")),
                Double.valueOf(getAccountManager().getAccountPersistence(player.getName(), "x")),
                Double.valueOf(getAccountManager().getAccountPersistence(player.getName(), "y")),
                Double.valueOf(getAccountManager().getAccountPersistence(player.getName(), "z")),
                Float.valueOf(getAccountManager().getAccountPersistence(player.getName(), "yaw")),
                Float.valueOf(getAccountManager().getAccountPersistence(player.getName(), "pitch"))
            ));
            
            getAccountManager().updateAccountPersistence(player.getName(), "waiting_room", "0");
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
    
    public boolean contains(Player player)
    {
        if (getAccountManager().isRegistered(player.getName()))
        {
            return "1".equals(getAccountManager().getAccountPersistence(player.getName(), "waiting_room"));
        }
        else
        {
            return players.contains(player);
        }
    }
    
    public org.bukkit.Location getWaitingRoomLocation()
    {
        return getConfig().getLocation("waiting-room.location").toBukkitLocation();
    }
    
    public void setWaitingRoomLocation(org.bukkit.Location location)
    {
        getConfig().set("waiting-room.location", Location.fromBukkitLocation(location));
    }
    
    public org.bukkit.Location getNewbieTeleportLocation()
    {
        String worldName = getConfig().getString("waiting-room.newbie-teleport.location.world");
        World  world = Bukkit.getServer().getWorld(worldName);
        double x = getConfig().getVector("waiting-room.newbie-teleport.location.position").getX();
        double y = getConfig().getVector("waiting-room.newbie-teleport.location.position").getY();
        double z = getConfig().getVector("waiting-room.newbie-teleport.location.position").getZ();
        float  yaw = (float) getConfig().getDouble("waiting-room.newbie-teleport.location.yaw");
        float  pitch = (float) getConfig().getDouble("waiting-room.newbie-teleport.location.pitch");
        
        return new org.bukkit.Location(world, x, y, z, yaw, pitch);
    }
    
    private final Set<Player> players = new HashSet<>();
}
