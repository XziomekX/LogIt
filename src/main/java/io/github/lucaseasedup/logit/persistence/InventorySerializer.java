/*
 * InventorySerializer.java
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
package io.github.lucaseasedup.logit.persistence;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.LogItPlugin;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.craftreflect.CraftInventoryCustom;
import io.github.lucaseasedup.logit.craftreflect.CraftReflect;
import io.github.lucaseasedup.logit.craftreflect.NBTTagCompound;
import io.github.lucaseasedup.logit.craftreflect.NBTTagList;
import it.sauronsoftware.base64.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Keys({
    @Key(name = "inv_contents", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "inv_armor", constraint = KeyConstraint.NOT_EMPTY),
})
@Before(LocationSerializer.class)
public final class InventorySerializer extends LogItCoreObject implements PersistenceSerializer
{
    @Override
    public void serialize(Map<String, String> data, Player player)
    {
        if (player.getWorld() != getWaitingRoomLocation().getWorld())
            return;
        
        try
        {
            data.put("inv_contents", serialize(getContentInventory(player.getInventory())));
            data.put("inv_armor", serialize(getArmorInventory(player.getInventory())));
            
            if (player.isOnline())
            {
                player.getInventory().clear();
                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);
            }
        }
        catch (ReflectiveOperationException ex)
        {
            log(Level.WARNING, "Could not serialize location for player: " + player.getName(), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, final Player player)
    {
        if (player.getWorld() != getWaitingRoomLocation().getWorld())
            return;
        
        try
        {
            final ItemStack[] contents = unserialize(data.get("inv_contents")).getContents();
            final ItemStack[] armor = unserialize(data.get("inv_armor")).getContents();
            
            if (player.isOnline())
            {
                player.getInventory().setContents(contents);
                player.getInventory().setArmorContents(armor);
            }
        }
        catch (ReflectiveOperationException ex)
        {
            log(Level.WARNING, "Could not unserialize location for player: " + player.getName(), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    @SuppressWarnings("unused")
    private String serialize(Inventory inventory) throws ReflectiveOperationException
    {
        CraftReflect reflect = LogItPlugin.getInstance().getCraftReflect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        NBTTagList itemList = reflect.newNBTTagList();
        
        for (int i = 0, n = inventory.getSize(); i < n; i++)
        {
            NBTTagCompound outputObject = reflect.newNBTTagCompound();
            reflect.saveItemStack(inventory.getItem(i), outputObject);
            itemList.add(outputObject);
        }
        
        itemList.write(dataOutput);
        
        return Base64.encode(outputStream.toString());
    }
    
    private Inventory unserialize(String data) throws ReflectiveOperationException
    {
        CraftReflect reflect = LogItPlugin.getInstance().getCraftReflect();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decode(data).getBytes());
        NBTTagList itemList = reflect.readNBTTag(inputStream).cast(NBTTagList.class);
        CraftInventoryCustom inventory = reflect.newCraftInventoryCustom(itemList.size());
        
        for (int i = 0, n = itemList.size(); i < n; i++)
        {
            NBTTagCompound inputObject = itemList.get(i).cast(NBTTagCompound.class);
            
            if (!inputObject.isEmpty())
            {
                inventory.setItem(i, reflect.createStack(inputObject));
            }
        }
        
        return (Inventory) inventory.getHolder().get();
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
        
        return (Inventory) storage.getHolder().get();
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
        
        return (Inventory) storage.getHolder().get();
    }
    
    private org.bukkit.Location getWaitingRoomLocation()
    {
        return getConfig().getLocation("waiting-room.location").toBukkitLocation();
    }
}
