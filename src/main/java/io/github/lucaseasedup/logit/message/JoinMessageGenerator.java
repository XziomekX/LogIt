package io.github.lucaseasedup.logit.message;

import static io.github.lucaseasedup.logit.message.MessageHelper.t;
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
        boolean beautifyMessages = core.getConfig("config.yml")
                .getBoolean("messages.beautify");
        String message;
        
        if (beautifyMessages)
        {
            message = t("join.beautified");
        }
        else
        {
            message = t("join.native");
        }
        
        String inWorld;
        
        if (beautifyMessages)
        {
            inWorld = t("join.beautified.inWorld");
        }
        else
        {
            inWorld = t("join.native.inWorld");
        }
        
        if (revealSpawnWorld)
        {
            String worldAlias = getWorldAlias(player.getWorld());
            
            message = message.replace("{1}",
                    inWorld.replace("{0}", worldAlias));
        }
        else
        {
            message = message.replace("{1}", "");
        }
        
        return message.replace("{0}", player.getName());
    }
    
    public static String getWorldAlias(World world)
    {
        LogItCore core = LogItCore.getInstance();
        
        if (!core.getConfig("config.yml").getBoolean("messages.multiverseHook")
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
