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
package io.github.lucaseasedup.logit.craftreflect.v1_6_R3;

import java.io.DataOutput;

public class NBTTagList extends io.github.lucaseasedup.logit.craftreflect.NBTTagList
{
    public NBTTagList()
    {
        super(new net.minecraft.server.v1_6_R3.NBTTagList());
    }
    
    @Override
    public void write(DataOutput d)
    {
        net.minecraft.server.v1_6_R3.NBTBase.a(getThis(), d);
    }
    
    @Override
    public void add(io.github.lucaseasedup.logit.craftreflect.NBTBase nbtb)
    {
        getThis().add((net.minecraft.server.v1_6_R3.NBTBase) nbtb.getHolder().get());
    }
    
    @Override
    public int size()
    {
        return getThis().size();
    }
    
    @Override
    public NBTBase get(int i)
    {
        return new NBTBase(getThis().get(i));
    }
    
    private net.minecraft.server.v1_6_R3.NBTTagList getThis()
    {
        return (net.minecraft.server.v1_6_R3.NBTTagList) getHolder().get();
    }
}
