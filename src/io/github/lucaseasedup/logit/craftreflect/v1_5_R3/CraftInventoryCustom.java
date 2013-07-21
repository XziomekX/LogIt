package io.github.lucaseasedup.logit.craftreflect.v1_5_R3;

import org.bukkit.inventory.ItemStack;

/**
 * @author LucasEasedUp
 */
public class CraftInventoryCustom extends io.github.lucaseasedup.logit.craftreflect.CraftInventoryCustom
{
    public CraftInventoryCustom(int size)
    {
        super(size);
        
        o = new org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryCustom(null, size);
    }
    
    @Override
    public void setItem(int i, ItemStack itemstack)
    {
        ((org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryCustom) o).setItem(i, itemstack);
    }
}
