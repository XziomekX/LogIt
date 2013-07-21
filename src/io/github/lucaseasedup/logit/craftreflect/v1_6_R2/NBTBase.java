package io.github.lucaseasedup.logit.craftreflect.v1_6_R2;

import java.io.DataOutput;

/**
 * @author LucasEasedUp
 */
public class NBTBase extends io.github.lucaseasedup.logit.craftreflect.NBTBase
{
    public NBTBase()
    {
        o = null;
    }
    
    public NBTBase(Object o)
    {
        this.o = o;
    }
    
    @Override
    public void write(DataOutput d)
    {
        net.minecraft.server.v1_6_R2.NBTBase.a((net.minecraft.server.v1_6_R2.NBTBase) o, d);
    }
}
