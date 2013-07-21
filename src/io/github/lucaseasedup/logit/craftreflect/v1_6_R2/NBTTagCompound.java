package io.github.lucaseasedup.logit.craftreflect.v1_6_R2;

import java.io.DataOutput;

/**
 * @author LucasEasedUp
 */
public class NBTTagCompound extends io.github.lucaseasedup.logit.craftreflect.NBTTagCompound
{
    public NBTTagCompound()
    {
        o = new net.minecraft.server.v1_6_R2.NBTTagCompound();
    }
    
    @Override
    public void write(DataOutput d)
    {
        net.minecraft.server.v1_6_R2.NBTBase.a((net.minecraft.server.v1_6_R2.NBTBase) o, d);
    }
    
    @Override
    public boolean isEmpty()
    {
        return ((net.minecraft.server.v1_6_R2.NBTTagCompound) o).isEmpty();
    }
}
