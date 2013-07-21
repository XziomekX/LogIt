package io.github.lucaseasedup.logit.craftreflect;

import org.bukkit.inventory.ItemStack;

/**
 * @author LucasEasedUp
 */
public abstract class CraftInventoryCustom extends ObjectWrapper
{
    public CraftInventoryCustom(int size)
    {
    }
    
    public abstract void setItem(int i, ItemStack itemstack);
}
