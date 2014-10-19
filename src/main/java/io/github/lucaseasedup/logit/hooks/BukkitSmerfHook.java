package io.github.lucaseasedup.logit.hooks;

import com.gmail.bukkitsmerf.check.Check;
import com.gmail.bukkitsmerf.check.IPlayersStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class BukkitSmerfHook
{
    private BukkitSmerfHook()
    {
    }
    
    /**
     * Uses BukkitSmerf to check if a player is premium.
     * 
     * @param player the player.
     * 
     * @return {@code true} if the player is premium;
     *         {@code false} if the player is non-premium
     *         or BukkitSmerf hasn't been found.
     */
    public static boolean isPremium(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (!Bukkit.getPluginManager().isPluginEnabled("BukkitSmerf"))
            return false;
        
        IPlayersStorage storage = Check.getStorage();
        Boolean isPremium = storage.isPremium(player.getName());
        
        return isPremium != null && isPremium.equals(true);
    }
}
