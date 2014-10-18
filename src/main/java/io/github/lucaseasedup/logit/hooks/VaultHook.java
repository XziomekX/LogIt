package io.github.lucaseasedup.logit.hooks;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class VaultHook
{
    private VaultHook()
    {
    }
    
    public static boolean isVaultEnabled()
    {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }
    
    public static void playerAddGroup(Player player, String group)
    {
        if (player == null || group == null)
            throw new IllegalArgumentException();
        
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault"))
            return;
        
        Bukkit.getServicesManager().getRegistration(Permission.class).getProvider()
                .playerAddGroup(player, group);
    }
    
    public static void playerRemoveGroup(Player player, String group)
    {
        if (player == null || group == null)
            throw new IllegalArgumentException();
        
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault"))
            return;
        
        Bukkit.getServicesManager().getRegistration(Permission.class).getProvider()
                .playerRemoveGroup(player, group);
    }
}
