/*
 * VaultHook.java
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
