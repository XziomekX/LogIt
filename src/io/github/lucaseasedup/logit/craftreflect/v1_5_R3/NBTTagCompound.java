package io.github.lucaseasedup.logit.craftreflect.v1_5_R3;

import java.io.DataOutput;

/**
 * @author LucasEasedUp
 */
public class NBTTagCompound extends io.github.lucaseasedup.logit.craftreflect.NBTTagCompound
{
    public NBTTagCompound()
    {
        o = new net.minecraft.server.v1_5_R3.NBTTagCompound();
    }
    
    @Override
    public void write(DataOutput d)
    {
        net.minecraft.server.v1_5_R3.NBTBase.a((net.minecraft.server.v1_5_R3.NBTBase) o, d);
    }
    
    @Override
    public boolean isEmpty()
    {
        return ((net.minecraft.server.v1_5_R3.NBTTagCompound) o).isEmpty();
    }
}
