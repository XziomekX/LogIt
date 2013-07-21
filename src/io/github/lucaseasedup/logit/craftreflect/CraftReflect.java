package io.github.lucaseasedup.logit.craftreflect;

import java.io.InputStream;
import org.bukkit.inventory.ItemStack;

/**
 * @author LucasEasedUp
 */
public interface CraftReflect
{
    public NBTTagList newNBTTagList();
    public NBTTagCompound newNBTTagCompound();
    public NBTBase readNBTTag(InputStream di);
    public CraftInventoryCustom newCraftInventoryCustom(int size);
    public void saveItemStack(ItemStack is, NBTTagCompound tagCompound);
    public ItemStack createStack(NBTTagCompound tagCompound);
}
