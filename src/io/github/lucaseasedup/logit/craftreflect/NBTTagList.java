package io.github.lucaseasedup.logit.craftreflect;

/**
 * @author LucasEasedUp
 */
public abstract class NBTTagList extends NBTBase
{
    public abstract void add(NBTBase nbtb);
    public abstract int size();
    public abstract NBTBase get(int i);
}
