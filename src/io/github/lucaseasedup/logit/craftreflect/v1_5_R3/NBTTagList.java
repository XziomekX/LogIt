package io.github.lucaseasedup.logit.craftreflect.v1_5_R3;

import java.io.DataOutput;

/**
 * @author LucasEasedUp
 */
public class NBTTagList extends io.github.lucaseasedup.logit.craftreflect.NBTTagList
{
    public NBTTagList()
    {
        o = new net.minecraft.server.v1_5_R3.NBTTagList();
    }
    
    @Override
    public void write(DataOutput d)
    {
        net.minecraft.server.v1_5_R3.NBTBase.a((net.minecraft.server.v1_5_R3.NBTBase) o, d);
    }
    
    @Override
    public void add(io.github.lucaseasedup.logit.craftreflect.NBTBase nbtb)
    {
        ((net.minecraft.server.v1_5_R3.NBTTagList) o).add((net.minecraft.server.v1_5_R3.NBTBase) nbtb.o);
    }
    
    @Override
    public int size()
    {
        return (((net.minecraft.server.v1_5_R3.NBTTagList) o)).size();
    }
    
    @Override
    public NBTBase get(int i)
    {
        return new NBTBase((((net.minecraft.server.v1_5_R3.NBTTagList) o)).get(i));
    }
}
