/*
 * CraftReflect.java
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
package io.github.lucaseasedup.logit.craftreflect.v1_5_R3;

import java.io.DataInputStream;
import java.io.InputStream;
import org.bukkit.inventory.ItemStack;

public class CraftReflect implements io.github.lucaseasedup.logit.craftreflect.CraftReflect
{
    @Override
    public io.github.lucaseasedup.logit.craftreflect.NBTTagList newNBTTagList()
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_5_R3.NBTTagList();
    }
    
    @Override
    public io.github.lucaseasedup.logit.craftreflect.NBTTagCompound newNBTTagCompound()
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_5_R3.NBTTagCompound();
    }
    
    @Override
    public io.github.lucaseasedup.logit.craftreflect.NBTBase readNBTTag(InputStream di)
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_5_R3.NBTBase(net.minecraft.server.v1_5_R3.NBTBase.b(new DataInputStream(di)));
    }
    
    @Override
    public io.github.lucaseasedup.logit.craftreflect.CraftInventoryCustom newCraftInventoryCustom(int size)
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_5_R3.CraftInventoryCustom(size);
    }
    
    @Override
    public void saveItemStack(ItemStack is, io.github.lucaseasedup.logit.craftreflect.NBTTagCompound tagCompound)
    {
        if (is == null)
            return;
        
        net.minecraft.server.v1_5_R3.ItemStack nmsCopy =
            org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack.asNMSCopy(is);
        nmsCopy.save((net.minecraft.server.v1_5_R3.NBTTagCompound) tagCompound.o);
    }
    
    @Override
    public ItemStack createStack(io.github.lucaseasedup.logit.craftreflect.NBTTagCompound tagCompound)
    {
        net.minecraft.server.v1_5_R3.NBTTagCompound nbtTagCompound =
                (net.minecraft.server.v1_5_R3.NBTTagCompound) tagCompound.o;
        net.minecraft.server.v1_5_R3.ItemStack is =
            net.minecraft.server.v1_5_R3.ItemStack.createStack(nbtTagCompound);
        
        return org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack.asBukkitCopy(is);
    }
}
