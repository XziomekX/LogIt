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

import io.github.lucaseasedup.logit.account.AccountManager;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author LucasEasedUp
 */
public class WaitingRoom
{
    public WaitingRoom(LogItCore core, AccountManager accountManager)
    {
        this.core = core;
        this.accountManager = accountManager;
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
        
        if (accountManager.isRegistered(player.getName()))
        {
            try
            {
                ReportedException.incrementRequestCount();
                
                accountManager.updatePersistenceLocation(player.getName(), player.getLocation());
                accountManager.updatePersistence(player.getName(), "waiting_room", "1");
            }
            catch (ReportedException ex)
            {
                core.log(Level.WARNING, "Could not update player waiting-room status.", ex);
                
                ex.rethrow();
            }
            finally
            {
                ReportedException.decrementRequestCount();
            }
        }
        
        player.teleport(getWaitingRoomLocation());
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
        if (!contains(player))
            return;
        
        if (accountManager.isRegistered(player.getName()))
        {
            player.teleport(accountManager.getPersistenceLocation(player.getName()));
            
            accountManager.updatePersistence(player.getName(), "waiting_room", "0");
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
        if (accountManager.isRegistered(player.getName()))
        {
            return "1".equals(accountManager.getPersistence(player.getName(), "waiting_room"));
        }
        else
        {
            return players.contains(player);
        }
    }
    
    public Location getWaitingRoomLocation()
    {
        World  world = Bukkit.getServer().getWorld(core.getConfig().getString("waiting-room.location.world"));
        double x = core.getConfig().getVector("waiting-room.location.position").getX();
        double y = core.getConfig().getVector("waiting-room.location.position").getY();
        double z = core.getConfig().getVector("waiting-room.location.position").getZ();
        float  yaw = (float) core.getConfig().getDouble("waiting-room.location.yaw");
        float  pitch = (float) core.getConfig().getDouble("waiting-room.location.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public void setWaitingRoomLocation(Location location)
    {
        core.getConfig().set("waiting-room.location.world", location.getWorld().getName());
        core.getConfig().set("waiting-room.location.position",
                new Vector(location.getX(), location.getY(), location.getZ()));
        core.getConfig().set("waiting-room.location.yaw", (double) location.getYaw());
        core.getConfig().set("waiting-room.location.pitch", (double) location.getPitch());
    }
    
    public Location getNewbieTeleportLocation()
    {
        String worldName = core.getConfig().getString("waiting-room.newbie-teleport.location.world");
        World  world = Bukkit.getServer().getWorld(worldName);
        double x = core.getConfig().getVector("waiting-room.newbie-teleport.location.position").getX();
        double y = core.getConfig().getVector("waiting-room.newbie-teleport.location.position").getY();
        double z = core.getConfig().getVector("waiting-room.newbie-teleport.location.position").getZ();
        float  yaw = (float) core.getConfig().getDouble("waiting-room.newbie-teleport.location.yaw");
        float  pitch = (float) core.getConfig().getDouble("waiting-room.newbie-teleport.location.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    private final LogItCore core;
    private final AccountManager accountManager;
    private final Set<Player> players = new HashSet<>();
}
