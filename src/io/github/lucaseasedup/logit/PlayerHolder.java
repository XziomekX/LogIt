/*
 * PlayerHolder.java
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * @author LucasEasedUp
 */
public final class PlayerHolder
{
    private PlayerHolder()
    {
    }
    
    public static Player[] getAll()
    {
        return players.toArray(new Player[players.size()]);
    }
    
    public static Player get(String name)
    {
        Player foundPlayer = null;
        String lowerCaseName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        
        for (Player player : players)
        {
            if (player.getName().toLowerCase().startsWith(lowerCaseName))
            {
                int curDelta = player.getName().length() - lowerCaseName.length();
                
                if (curDelta < delta)
                {
                    foundPlayer = player;
                    delta = curDelta;
                }
                
                if (curDelta == 0)
                {
                    break;
                }
            }
        }
        
        return foundPlayer;
    }
    
    public static Player getExact(String name)
    {
        for (Player player : players)
        {
            if (player.getName().equalsIgnoreCase(name))
            {
                return player;
            }
        }
        
        return null;
    }
    
    public static void release(Player player)
    {
        players.remove(player);
    }

    protected static void registerListenerForPlugin(Plugin plugin)
    {
        Bukkit.getPluginManager().registerEvents(new Listener()
        {
            @EventHandler(priority = EventPriority.MONITOR)
            private void onPlayerJoin(PlayerJoinEvent event)
            {
                Player player = event.getPlayer();
                
                if (!players.contains(player))
                {
                    players.add(player);
                }
            }
        }, plugin);
    }
    
    public static final Set<Player> players = new HashSet<>();
}
