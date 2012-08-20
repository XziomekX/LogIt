package com.gmail.lucaseasedup.logit;

import static com.gmail.lucaseasedup.logit.LogItPlugin.formatColorCodes;
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
        core = LogItCore.getInstance();
    }
    
    public String generate(Player player)
    {
        if (!core.getConfig().getShowSpawnWorldInfo())
            return "";
        
        return " " + formatColorCodes(getMessage("IN_WORLD", true).replace("%world%", getWorldAlias(player.getWorld())));
    }
    
    protected String getWorldAlias(World world)
    {
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        
        if (p == null)
            return world.getName();
        
        return ((MultiverseCore) p).getMVWorldManager().getMVWorld(world).getAlias();
    }
    
    public static SpawnWorldInfoGenerator getInstance()
    {
        return SpawnWorldInfoGenerator.WorldInfoGeneratorHolder.INSTANCE;
    }
    
    private static class WorldInfoGeneratorHolder
    {
        private static final SpawnWorldInfoGenerator INSTANCE = new SpawnWorldInfoGenerator();
    }
    
    private LogItCore core;
}
