/*
 * SpawnWorldInfoGenerator.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit;

import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class SpawnWorldInfoGenerator
{
    private SpawnWorldInfoGenerator()
    {
    }
    
    public static String generate(Player player)
    {
        if (!LogItCore.getInstance().getConfig().getShowSpawnWorldInfo())
        {
            return "";
        }
        
        return " " + getMessage("IN_WORLD").replace("%world%", getWorldAlias(player.getWorld()));
    }
    
    protected static String getWorldAlias(World world)
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
        {
            return world.getName();
        }
        
        return ((MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core")).getMVWorldManager().getMVWorld(world).getAlias();
    }
}
