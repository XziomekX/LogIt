package io.github.lucaseasedup.logit.storage;

import java.util.ArrayList;
import java.util.List;

public final class NullStorage implements Storage
{
    @Override
    public void connect()
    {
    }
    
    @Override
    public boolean isConnected()
    {
        return true;
    }
    
    @Override
    public void ping()
    {
    }
    
    @Override
    public void close()
    {
    }
    
    @Override
    public List<String> getUnitNames()
    {
        return new ArrayList<>();
    }
    
    @Override
    public UnitKeys getKeys(String unit)
    {
        return new UnitKeys();
    }
    
    @Override
    public String getPrimaryKey(String unit)
    {
        return null;
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit)
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, Selector selector)
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, List<String> keys)
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<StorageEntry> selectEntries(
            String unit, List<String> keys, Selector selector
    )
    {
        return new ArrayList<>();
    }
    
    @Override
    public void createUnit(String unit, UnitKeys keys, String primaryKey)
    {
    }
    
    @Override
    public void renameUnit(String unit, String newName)
    {
    }
    
    @Override
    public void eraseUnit(String unit)
    {
    }
    
    @Override
    public void removeUnit(String unit)
    {
    }
    
    @Override
    public void addKey(String unit, String key, DataType type)
    {
    }
    
    @Override
    public void addEntry(String unit, StorageEntry entry)
    {
    }
    
    @Override
    public void updateEntries(
            String unit, StorageEntry entrySubset, Selector selector
    )
    {
    }
    
    @Override
    public void removeEntries(String unit, Selector selector)
    {
    }
    
    @Override
    public boolean isAutobatchEnabled()
    {
        return false;
    }
    
    @Override
    public void setAutobatchEnabled(boolean status)
    {
        // Batching is not supported.
    }
    
    @Override
    public void executeBatch()
    {
        // Batching is not supported.
    }
    
    @Override
    public void clearBatch()
    {
        // Batching is not supported.
    }
}
