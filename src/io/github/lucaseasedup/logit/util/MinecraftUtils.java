/*
 * MinecraftUtils.java
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
package io.github.lucaseasedup.logit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
public final class MinecraftUtils
{
    private MinecraftUtils()
    {
    }
    
    public static File getPlayerFile(String world, String username)
    {
        File worldFolder = Bukkit.getWorld(world).getWorldFolder();
        File playerFile = new File(worldFolder, "players/" + username + ".dat");
        
        return playerFile;
    }
    
    public static void saveInventory(String world,
                                     String username,
                                     Inventory contentsInventory,
                                     Inventory armorInventory) throws IOException
    {
        File playerFile = getPlayerFile(world, username);
        
        if (!playerFile.exists())
            throw new FileNotFoundException();
        
        ItemStack[] contents = contentsInventory.getContents();
        ItemStack[] armor = armorInventory.getContents();
        
        Map<String, Tag> rootTagMap = readRootTagMap(new FileInputStream(playerFile));
        List<Tag>  inventoryTagList = new ArrayList<>();
        
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

        rootTagMap.put("Inventory", new ListTag("Inventory", CompoundTag.class, inventoryTagList));

        writeRootTagMap(new FileOutputStream(playerFile), rootTagMap);
    }
    
    public static void saveAir(String world, String username, short air) throws IOException
    {
        File playerFile = getPlayerFile(world, username);
        
        if (!playerFile.exists())
            throw new FileNotFoundException();
        
        Map<String, Tag> rootTagMap = readRootTagMap(new FileInputStream(playerFile));
        
        rootTagMap.put("Air", new ShortTag("Air", air));
        
        writeRootTagMap(new FileOutputStream(playerFile), rootTagMap);
    }
    
    private static Map<String, Tag> readRootTagMap(InputStream is) throws IOException
    {
        CompoundTag rootCompoundTag;
        
        try (NBTInputStream nbtIs = new NBTInputStream(is))
        {
            rootCompoundTag = (CompoundTag) nbtIs.readTag();
        }
        
        Map<String, Tag> rootTagMap = new HashMap<>();
        
        for (Entry<String, Tag> tag : rootCompoundTag.getValue().entrySet())
        {
            rootTagMap.put(tag.getKey(), tag.getValue());
        }
        
        return rootTagMap;
    }
    
    private static void writeRootTagMap(OutputStream os, Map<String, Tag> rootTagMap) throws IOException
    {
        try (NBTOutputStream nbtOs = new NBTOutputStream(os))
        {
            nbtOs.writeTag(new CompoundTag("", rootTagMap));
        }
    }
}
