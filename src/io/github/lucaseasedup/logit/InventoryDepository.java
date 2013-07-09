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
package io.github.lucaseasedup.logit;

import io.github.lucaseasedup.logit.db.Database;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_5_R3.NBTBase;
import net.minecraft.server.v1_5_R3.NBTTagCompound;
import net.minecraft.server.v1_5_R3.NBTTagList;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author LucasEasedUp
 */
public class InventoryDepository
{
    public InventoryDepository(Database inventoryDatabase)
    {
        this.inventoryDatabase = inventoryDatabase;
    }
    
    /**
     * Deposits player's items and armor.
     * <p/>
     * If the player's inventory has already been deposited, no action will be taken.
     * 
     * @param player Player.
     */
    public void deposit(Player player)
    {
        if (contents.containsKey(player))
            return;
        
        try
        {
            ResultSet rs = inventoryDatabase.select("inventories", new String[]{"username"},
                new String[]{"username", "=", player.getName().toLowerCase()});
            
            if (!rs.isBeforeFirst())
            {
                inventoryDatabase.insert("inventories", new String[]{
                    "username",
                    "inv-contents",
                    "inv-armor"
                }, new String[]{
                    player.getName().toLowerCase(),
                    toBase64(getContentInventory(player.getInventory())),
                    toBase64(getArmorInventory(player.getInventory()))
                });
            }
            else
            {
                inventoryDatabase.update("inventories", new String[]{
                    "username", "=", player.getName().toLowerCase()
                }, new String[]{
                    "inv-contents", toBase64(getContentInventory(player.getInventory())),
                    "inv-armor", toBase64(getArmorInventory(player.getInventory()))
                });
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(InventoryDepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        contents.put(player, player.getInventory().getContents().clone());
        armorContents.put(player, player.getInventory().getArmorContents().clone());
        
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }
    
    /**
     * Withdraws players's items and armor.
     * <p/>
     * If the player's inventory has not been deposited, no action will be taken.
     * 
     * @param player Player.
     */
    public void withdraw(Player player)
    {
        if (!contents.containsKey(player))
            return;
        
        try
        {        
            ResultSet rs = inventoryDatabase.select("inventories", new String[]{
                "username", "inv-contents", "inv-armor"
            }, new String[]{
                "username", "=", player.getName().toLowerCase()
            });
            
            player.getInventory().setContents(fromBase64(rs.getString("inv-contents")).getContents());
            player.getInventory().setArmorContents(fromBase64(rs.getString("inv-armor")).getContents());
            
            inventoryDatabase.delete("inventories", new String[]{
                "username", "=", player.getName().toLowerCase()
            });
        }
        catch (SQLException ex)
        {
            Logger.getLogger(InventoryDepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        player.getInventory().setContents(contents.remove(player));
        player.getInventory().setArmorContents(armorContents.remove(player));
    }
    
    public Database getInventoryDatabase()
    {
        return inventoryDatabase;
    }
    
    public static Inventory getArmorInventory(PlayerInventory inventory)
    {
        ItemStack[] armor = inventory.getArmorContents();
        CraftInventoryCustom storage = new CraftInventoryCustom(null, armor.length);
        
        for (int i = 0; i < armor.length; i++)
            storage.setItem(i, armor[i]);
        
        return storage;
    }
    
    public static Inventory getContentInventory(PlayerInventory inventory)
    {
        ItemStack[] content = inventory.getContents();
        CraftInventoryCustom storage = new CraftInventoryCustom(null, content.length);
        
        for (int i = 0; i < content.length; i++)
            storage.setItem(i, content[i]);
        
        return storage;
    }
    
    public static String toBase64(Inventory inventory)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        NBTTagList itemList = new NBTTagList();
        
        for (int i = 0; i < inventory.getSize(); i++)
        {
            NBTTagCompound outputObject = new NBTTagCompound();
            net.minecraft.server.v1_5_R3.ItemStack craft = getCraftVersion(inventory.getItem(i));
            
            if (craft != null)
                craft.save(outputObject);
            
            itemList.add(outputObject);
        }
        
        // Save the list
        NBTBase.a(itemList, dataOutput);
        
        return Base64.encodeBase64String(outputStream.toByteArray());
    }
    
    public static Inventory fromBase64(String data)
    {
        ByteArrayInputStream inputStream = new
        ByteArrayInputStream(Base64.decodeBase64(data));
        NBTTagList itemList = (NBTTagList) NBTBase.b(new DataInputStream(inputStream));
        Inventory inventory = new CraftInventoryCustom(null, itemList.size());
        
        for (int i = 0; i < itemList.size(); i++)
        {
            NBTTagCompound inputObject = (NBTTagCompound) itemList.get(i);
            
            if (!inputObject.isEmpty())
            {
                inventory.setItem(i, CraftItemStack.asBukkitCopy(net.minecraft.server.v1_5_R3.ItemStack.createStack(inputObject)));
            }
        }
        
        return inventory;
    }
    
    private static net.minecraft.server.v1_5_R3.ItemStack getCraftVersion(ItemStack stack)
    {
        if (stack != null)
            return CraftItemStack.asNMSCopy(stack);
        
        return null;
    }
    
    private final Database inventoryDatabase;
    
    private final HashMap<Player, ItemStack[]> contents = new HashMap<>();
    private final HashMap<Player, ItemStack[]> armorContents = new HashMap<>();
}
