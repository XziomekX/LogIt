/*
 * BukkitSmerfHook.java
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
        
        return isPremium != null && isPremium == Boolean.TRUE;
    }
}
