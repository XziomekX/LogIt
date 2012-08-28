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
import org.bukkit.plugin.Plugin;

/**
 * @author LucasEasedUp
 */
public class SpawnWorldInfoGenerator
{
    private SpawnWorldInfoGenerator()
    {
    }
    
    public String generate(Player player)
    {
        if (!LogItCore.getInstance().getConfig().getShowSpawnWorldInfo())
        {
            return "";
        }
        
        return " " + getMessage("IN_WORLD").replace("%world%", getWorldAlias(player.getWorld()));
    }
    
    protected String getWorldAlias(World world)
    {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        
        if (plugin == null)
        {
            return world.getName();
        }
        else
        {
            return ((MultiverseCore) plugin).getMVWorldManager().getMVWorld(world).getAlias();
        }
    }
    
    public static SpawnWorldInfoGenerator getInstance()
    {
        return SpawnWorldInfoGenerator.WorldInfoGeneratorHolder.INSTANCE;
    }
    
    private static class WorldInfoGeneratorHolder
    {
        private static final SpawnWorldInfoGenerator INSTANCE = new SpawnWorldInfoGenerator();
    }
}
