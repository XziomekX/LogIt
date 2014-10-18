package io.github.lucaseasedup.logit.hooks;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class EssentialsHook
{
    private EssentialsHook()
    {
    }
    
    public static boolean isVanished(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (!Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            return false;
        
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        
        return ess.getUser(player).isVanished();
    }
}
