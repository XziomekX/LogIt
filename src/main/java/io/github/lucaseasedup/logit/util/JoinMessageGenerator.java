/*
 * JoinMessageGenerator.java
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
package io.github.lucaseasedup.logit.util;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class JoinMessageGenerator
{
    private JoinMessageGenerator()
    {
    }
    
    public static String generate(Player player, boolean revealSpawnWorld)
    {
        String message = getMessage("JOIN").replace("%player%", player.getName());
        
        if (revealSpawnWorld)
        {
            message = message.replace("%in_world%",
                    getMessage("IN_WORLD").replace("%world%", getWorldAlias(player.getWorld())));
        }
        else
        {
            message = message.replace("%in_world%", "");
        }
        
        return message;
    }
    
    public static String getWorldAlias(World world)
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
            return world.getName();
        
        MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        
        return multiverseCore.getMVWorldManager().getMVWorld(world).getAlias();
    }
}
