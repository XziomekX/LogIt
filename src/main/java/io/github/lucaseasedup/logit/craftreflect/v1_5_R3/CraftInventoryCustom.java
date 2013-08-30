/*
 * CraftInventoryCustom.java
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

import org.bukkit.inventory.ItemStack;

public class CraftInventoryCustom extends io.github.lucaseasedup.logit.craftreflect.CraftInventoryCustom
{
    public CraftInventoryCustom(int size)
    {
        super(new org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryCustom(null, size));
    }
    
    @Override
    public void setItem(int i, ItemStack itemstack)
    {
        getThis().setItem(i, itemstack);
    }
    
    private org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryCustom getThis()
    {
        return (org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryCustom) getHolder().get();
    }
}
