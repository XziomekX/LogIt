package io.github.lucaseasedup.logit.craftreflect;

import java.io.DataOutput;

/**
 * @author LucasEasedUp
 */
public abstract class NBTBase extends ObjectWrapper
{
    public abstract void write(DataOutput d);
}
