/*
 * InventoryDepository.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
package io.github.lucaseasedup.logit.inventory;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.LogItPlugin;
import io.github.lucaseasedup.logit.craftreflect.CraftInventoryCustom;
import io.github.lucaseasedup.logit.craftreflect.CraftReflect;
import io.github.lucaseasedup.logit.craftreflect.NBTTagCompound;
import io.github.lucaseasedup.logit.craftreflect.NBTTagList;
import io.github.lucaseasedup.logit.db.Database;
import it.sauronsoftware.base64.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.ShortTag;
import org.jnbt.Tag;

/**
 * @author LucasEasedUp
 */
public final class InventoryDepository extends LogItCoreObject
{
    public InventoryDepository(LogItCore core, Database inventoryDatabase)
    {
        super(core);
        
        this.inventoryDatabase = inventoryDatabase;
    }
    
    /**
     * Deposits player's items and armor.
     * <p/>
     * If the player's inventory has already been deposited, no action will be taken.
     * 
     * @param player Player.
     */
    public void deposit(Player player) throws InventorySerializationException
    {
        if (players.contains(player))
            return;
        
        try
        {
            List<Map<String, String>> rs = inventoryDatabase.select("inventories", new String[]{"username"},
                new String[]{"username", "=", player.getName().toLowerCase()});
            
            if (rs.isEmpty())
            {
                inventoryDatabase.insert("inventories", new String[]{
                    "username",
                    "world",
                    "inv_contents",
                    "inv_armor"
                }, new String[]{
                    player.getName().toLowerCase(),
                    player.getWorld().getName(),
                    serialize(getContentInventory(player.getInventory())),
                    serialize(getArmorInventory(player.getInventory()))
                });
            }
            else
            {
                inventoryDatabase.update("inventories", new String[]{
                    "username", "=", player.getName().toLowerCase()
                }, new String[]{
                    "world", player.getWorld().getName(),
                    "inv_contents", serialize(getContentInventory(player.getInventory())),
                    "inv_armor", serialize(getArmorInventory(player.getInventory()))
                });
            }
        }
        catch (SQLException | ReflectiveOperationException ex)
        {
            throw new InventorySerializationException(ex);
        }
        
        players.add(player);
        
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
    public void withdraw(Player player) throws InventorySerializationException
    {
        if (!players.contains(player))
            return;
        
        try
        {        
            List<Map<String, String>> rs = inventoryDatabase.select("inventories", new String[]{
                "username", "world", "inv_contents", "inv_armor"
            }, new String[]{
                "username", "=", player.getName().toLowerCase()
            });
            
            if (!rs.isEmpty())
            {
                if (rs.get(0).get("world").equalsIgnoreCase(player.getWorld().getName()))
                {
                    player.getInventory().setContents(unserialize(rs.get(0).get("inv_contents")).getContents());
                    player.getInventory().setArmorContents(unserialize(rs.get(0).get("inv_armor")).getContents());

                    inventoryDatabase.delete("inventories", new String[]{
                        "username", "=", player.getName().toLowerCase()
                    });
                }
            }
        }
        catch (SQLException | ReflectiveOperationException ex)
        {
            throw new InventorySerializationException(ex);
        }
        
        players.remove(player);
    }
    
    public void saveInventory(String world, String username, Inventory contentsInventory, Inventory armorInventory)
        throws IOException
    {
        File playerFile = new File(System.getProperty("user.dir") + "/" + world + "/players/" + username + ".dat");

        if (!playerFile.exists())
            throw new FileNotFoundException();
        
        ItemStack[] contents = contentsInventory.getContents();
        ItemStack[] armor = armorInventory.getContents();
        CompoundTag rootCompoundTag;
        
        try (NBTInputStream is = new NBTInputStream(new FileInputStream(playerFile)))
        {
            rootCompoundTag = (CompoundTag) is.readTag();
        }

        HashMap<String, Tag> newRootTagMap = new HashMap<>();
        
        for (Entry<String, Tag> tag : rootCompoundTag.getValue().entrySet())
        {
            newRootTagMap.put(tag.getKey(), tag.getValue());
        }
        
        List<Tag> inventoryTagList = new ArrayList<>();

        for (int i = 0; i < 36; i++)
        {
            if (contents[i] != null)
            {
                HashMap<String, Tag> tagMap = new HashMap<>();
                tagMap.put("id", new ShortTag("id", (short) contents[i].getTypeId()));
                tagMap.put("Damage", new ShortTag("Damage", contents[i].getDurability()));
                tagMap.put("Count", new ByteTag("Count", (byte) contents[i].getAmount()));
                tagMap.put("Slot", new ByteTag("Slot", (byte) i));

                if (contents[i].getTypeId() > 0)
                {
                    inventoryTagList.add(new CompoundTag("", tagMap));
                }
            }
        }

        for (int i = 0; i < 4; i++)
        {
            if (armor[i] != null)
            {
                HashMap<String, Tag> tagMap = new HashMap<>();
                tagMap.put("id", new ShortTag("id", (short) armor[i].getTypeId()));
                tagMap.put("Damage", new ShortTag("Damage", armor[i].getDurability()));
                tagMap.put("Count", new ByteTag("Count", (byte) 1));
                tagMap.put("Slot", new ByteTag("Slot", (byte) (i + 100)));

                if (armor[i].getTypeId() > 0)
                {
                    inventoryTagList.add(new CompoundTag("", tagMap));
                }
            }
        }

        newRootTagMap.put("Inventory", new ListTag("Inventory", CompoundTag.class, inventoryTagList));

        try (NBTOutputStream os = new NBTOutputStream(new FileOutputStream(playerFile)))
        {
            os.writeTag(new CompoundTag("", newRootTagMap));
        }
    }
    
    public String serialize(Inventory inventory) throws ReflectiveOperationException
    {
        CraftReflect reflect = LogItPlugin.getInstance().getCraftReflect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        NBTTagList itemList = reflect.newNBTTagList();
        
        for (int i = 0; i < inventory.getSize(); i++)
        {
            NBTTagCompound outputObject = reflect.newNBTTagCompound();
            reflect.saveItemStack(inventory.getItem(i), outputObject);
            itemList.add(outputObject);
        }
        
        itemList.write(dataOutput);
        
        return Base64.encode(outputStream.toString());
    }
    
    public Inventory unserialize(String data) throws ReflectiveOperationException
    {
        CraftReflect reflect = LogItPlugin.getInstance().getCraftReflect();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decode(data).getBytes());
        NBTTagList itemList = reflect.readNBTTag(inputStream).cast(NBTTagList.class);
        CraftInventoryCustom inventory = reflect.newCraftInventoryCustom(itemList.size());
        
        for (int i = 0; i < itemList.size(); i++)
        {
            NBTTagCompound inputObject = itemList.get(i).cast(NBTTagCompound.class);
            
            if (!inputObject.isEmpty())
            {
                inventory.setItem(i, reflect.createStack(inputObject));
            }
        }
        
        return (Inventory) inventory.o;
    }
    
    public Database getInventoryDatabase()
    {
        return inventoryDatabase;
    }
    
    private Inventory getArmorInventory(PlayerInventory inventory)
    {
        CraftReflect reflect = LogItPlugin.getInstance().getCraftReflect();
        ItemStack[] armor = inventory.getArmorContents();
        CraftInventoryCustom storage = reflect.newCraftInventoryCustom(armor.length);
        
        for (int i = 0; i < armor.length; i++)
        {
            storage.setItem(i, armor[i]);
        }
        
        return (Inventory) storage.o;
    }
    
    private Inventory getContentInventory(PlayerInventory inventory)
    {
        CraftReflect reflect = LogItPlugin.getInstance().getCraftReflect();
        ItemStack[] content = inventory.getContents();
        CraftInventoryCustom storage = reflect.newCraftInventoryCustom(content.length);
        
        for (int i = 0; i < content.length; i++)
        {
            storage.setItem(i, content[i]);
        }
        
        return (Inventory) storage.o;
    }
    
    private final Database inventoryDatabase;
    private final List<Player> players = new ArrayList<>();
}
