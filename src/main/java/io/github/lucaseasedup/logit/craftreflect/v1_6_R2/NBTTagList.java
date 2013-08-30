/*
 * NBTTagList.java
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
package io.github.lucaseasedup.logit.craftreflect.v1_6_R2;

import java.io.DataOutput;

public class NBTTagList extends io.github.lucaseasedup.logit.craftreflect.NBTTagList
{
    public NBTTagList()
    {
        super(new net.minecraft.server.v1_6_R2.NBTTagList());
    }
    
    @Override
    public void write(DataOutput d)
    {
        net.minecraft.server.v1_6_R2.NBTBase.a((net.minecraft.server.v1_6_R2.NBTBase) getHolder().get(), d);
    }
    
    @Override
    public void add(io.github.lucaseasedup.logit.craftreflect.NBTBase nbtb)
    {
        ((net.minecraft.server.v1_6_R2.NBTTagList) getHolder().get()).add((net.minecraft.server.v1_6_R2.NBTBase) nbtb.getHolder().get());
    }
    
    @Override
    public int size()
    {
        return (((net.minecraft.server.v1_6_R2.NBTTagList) getHolder().get())).size();
    }
    
    @Override
    public NBTBase get(int i)
    {
        return new NBTBase((((net.minecraft.server.v1_6_R2.NBTTagList) getHolder().get())).get(i));
    }
}
