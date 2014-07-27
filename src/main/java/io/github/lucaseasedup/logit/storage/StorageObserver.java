package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.storage.Storage.DataType;
import java.util.Hashtable;

public abstract class StorageObserver
{
    public void beforeClose()
    {
    }
    
    public void afterCreateUnit(String unit, Hashtable<String, DataType> keys)
    {
    }
    
    public void afterRenameUnit(String unit, String newName)
    {
    }
    
    public void afterEraseUnit(String unit)
    {
    }
    
    public void afterRemoveUnit(String unit)
    {
    }
    
    public void afterAddKey(String unit, String key, DataType type)
    {
    }
    
    public void afterAddEntry(String unit, Storage.Entry entry)
    {
    }
    
    public void afterUpdateEntries(String unit, Storage.Entry entrySubset, Selector selector)
    {
    }
    
    public void afterRemoveEntries(String unit, Selector selector)
    {
    }
}
