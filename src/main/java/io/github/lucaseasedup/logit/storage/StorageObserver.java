package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.storage.Storage.DataType;
import java.util.Hashtable;

public abstract class StorageObserver
{
    public void beforeClose()
    {
    }
    
    @SuppressWarnings("unused")
    public void afterCreateUnit(String unit, Hashtable<String, DataType> keys)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterRenameUnit(String unit, String newName)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterEraseUnit(String unit)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterRemoveUnit(String unit)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterAddKey(String unit, String key, DataType type)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterAddEntry(String unit, Storage.Entry entry)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterUpdateEntries(String unit, Storage.Entry entrySubset, Selector selector)
    {
    }
    
    @SuppressWarnings("unused")
    public void afterRemoveEntries(String unit, Selector selector)
    {
    }
}
