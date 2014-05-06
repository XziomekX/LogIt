/*
 * JoinMessageGenerator.java
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
package io.github.lucaseasedup.logit.util;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import com.onarandombox.MultiverseCore.MultiverseCore;
import io.github.lucaseasedup.logit.LogItCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class JoinMessageGenerator
{
    private JoinMessageGenerator()
    {
    }
    
    public static String generate(Player player, boolean revealSpawnWorld)
    {
        LogItCore core = LogItCore.getInstance();
        
        assert core != null;
        
        String message = getMessage("JOIN");
        
        if (core.getConfig().getBoolean("messages.beautify"))
        {
            message = getMessage("JOIN");
        }
        else
        {
            message = "\u00A7e%player% joined the game%in_world%.";
        }
        
        String inWorld;
        
        if (core.getConfig().getBoolean("messages.beautify"))
        {
            inWorld = getMessage("IN_WORLD");
        }
        else
        {
            inWorld = " (in \"\u00A76%world%\u00A7e\")";
        }
        
        if (revealSpawnWorld)
        {
            String worldAlias = getWorldAlias(player.getWorld());
            
            message = message.replace("%in_world%", inWorld.replace("%world%", worldAlias));
        }
        else
        {
            message = message.replace("%in_world%", "");
        }
        
        return message.replace("%player%", player.getName());
    }
    
    public static String getWorldAlias(World world)
    {
        if (!LogItCore.getInstance().getConfig().getBoolean("messages.multiverse-hook")
                || !Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
        {
            return world.getName();
        }
        
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        
        if (!(plugin instanceof MultiverseCore))
            return world.getName();
        
        return ((MultiverseCore) plugin).getMVWorldManager().getMVWorld(world).getAlias();
    }
}
