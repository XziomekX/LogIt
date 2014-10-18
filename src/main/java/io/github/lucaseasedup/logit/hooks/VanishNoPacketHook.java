package io.github.lucaseasedup.logit.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishPlugin;

public final class VanishNoPacketHook
{
    private VanishNoPacketHook()
    {
    }
    
    public static boolean isVanished(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (!Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket"))
            return false;
        
        VanishPlugin plugin =
                (VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket");
        
        return plugin.getManager().isVanished(player);
    }
}
