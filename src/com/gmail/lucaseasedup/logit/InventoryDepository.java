/*
 * InventoryDepository.java
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

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author LucasEasedUp
 */
public class InventoryDepository
{
    /**
     * Deposits player's items and armor. If the player's inventory has already
     * been deposited, no action will be taken.
     * 
     * @param player Player.
     */
    public void deposit(Player player)
    {
        if (contents.containsKey(player))
            return;
        
        contents.put(player, player.getInventory().getContents().clone());
        armorContents.put(player, player.getInventory().getArmorContents().clone());
        
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }
    
    /**
     * Withdraws players's items and armor. If the player's inventory has not been
     * deposited, no action will be taken.
     * 
     * @param player Player.
     */
    public void withdraw(Player player)
    {
        if (!contents.containsKey(player))
            return;
        
        player.getInventory().setContents(contents.remove(player));
        player.getInventory().setArmorContents(armorContents.remove(player));
    }
    
    private final HashMap<Player, ItemStack[]> contents = new HashMap<>();
    private final HashMap<Player, ItemStack[]> armorContents = new HashMap<>();
}
