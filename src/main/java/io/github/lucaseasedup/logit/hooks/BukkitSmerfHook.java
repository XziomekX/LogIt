package io.github.lucaseasedup.logit.hooks;

import com.gmail.bukkitsmerf.autoin.api.AutoInAPI;
import com.gmail.bukkitsmerf.autoin.api.PlayerStatus;
import com.gmail.bukkitsmerf.check.Check;
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
        
        if (Bukkit.getPluginManager().isPluginEnabled("BukkitSmerf"))
        {
            com.gmail.bukkitsmerf.check.IPlayersStorage storage =
                    Check.getStorage();
            Boolean isPremium = storage.isPremium(player.getName());
            
            return isPremium != null && isPremium.equals(true);
        }
        else if (Bukkit.getPluginManager().isPluginEnabled("AutoIn_BukkitSmerf"))
        {
            com.gmail.bukkitsmerf.autoin.api.IPlayersStorage storage =
                    AutoInAPI.getStorage();
            PlayerStatus status = storage.getStatus(player);
            
            return status.isPremium();
        }
        else
        {
            return false;
        }
    }
}
