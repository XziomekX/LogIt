package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.storage.Storage.Type;
import java.util.Hashtable;

public interface StorageObserver
{
    public abstract void createUnit(String unit, Hashtable<String, Type> keys);
    public abstract void renameUnit(String unit, String newName);
    public abstract void eraseUnit(String unit);
    public abstract void removeUnit(String unit);
    
    public abstract void addKey(String unit, String key, Type type);
    public abstract void addEntry(String unit, Storage.Entry entry);
    public abstract void updateEntries(String unit, Storage.Entry entrySubset, Selector selector);
    public abstract void removeEntries(String unit, Selector selector);
}
