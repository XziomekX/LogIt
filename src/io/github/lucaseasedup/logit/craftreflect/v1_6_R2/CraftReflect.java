package io.github.lucaseasedup.logit.craftreflect.v1_6_R2;

import java.io.DataInputStream;
import java.io.InputStream;
import org.bukkit.inventory.ItemStack;

/**
 * @author LucasEasedUp
 */
public class CraftReflect implements io.github.lucaseasedup.logit.craftreflect.CraftReflect
{
    @Override
    public io.github.lucaseasedup.logit.craftreflect.NBTTagList newNBTTagList()
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_6_R2.NBTTagList();
    }
    
    @Override
    public io.github.lucaseasedup.logit.craftreflect.NBTTagCompound newNBTTagCompound()
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_6_R2.NBTTagCompound();
    }
    
    @Override
    public io.github.lucaseasedup.logit.craftreflect.NBTBase readNBTTag(InputStream di)
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_6_R2.NBTBase(net.minecraft.server.v1_6_R2.NBTBase.a(new DataInputStream(di)));
    }
    
    @Override
    public io.github.lucaseasedup.logit.craftreflect.CraftInventoryCustom newCraftInventoryCustom(int size)
    {
        return new io.github.lucaseasedup.logit.craftreflect.v1_6_R2.CraftInventoryCustom(size);
    }
    
    @Override
    public void saveItemStack(ItemStack is, io.github.lucaseasedup.logit.craftreflect.NBTTagCompound tagCompound)
    {
        if (is == null)
            return;
        
        net.minecraft.server.v1_6_R2.ItemStack nmsCopy =
            org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack.asNMSCopy(is);
        nmsCopy.save((net.minecraft.server.v1_6_R2.NBTTagCompound) tagCompound.o);
    }
    
    @Override
    public ItemStack createStack(io.github.lucaseasedup.logit.craftreflect.NBTTagCompound tagCompound)
    {
        net.minecraft.server.v1_6_R2.ItemStack is =
            net.minecraft.server.v1_6_R2.ItemStack.createStack((net.minecraft.server.v1_6_R2.NBTTagCompound) tagCompound.o);
        
        return org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack.asBukkitCopy(is);
    }
}
