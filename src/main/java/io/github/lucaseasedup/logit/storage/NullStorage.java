package io.github.lucaseasedup.logit.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class NullStorage implements Storage
{
    @Override
    public void connect() throws IOException
    {
    }
    
    @Override
    public boolean isConnected() throws IOException
    {
        return true;
    }
    
    @Override
    public void ping() throws IOException
    {
    }
    
    @Override
    public void close() throws IOException
    {
    }
    
    @Override
    public List<String> getUnitNames() throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public UnitKeys getKeys(String unit) throws IOException
    {
        return new UnitKeys();
    }
    
    @Override
    public String getPrimaryKey(String unit) throws IOException
    {
        return null;
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit) throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, Selector selector)
            throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<StorageEntry> selectEntries(
            String unit, List<String> keys, Selector selector
    ) throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public void createUnit(String unit, UnitKeys keys, String primaryKey)
            throws IOException
    {
    }
    
    @Override
    public void renameUnit(String unit, String newName) throws IOException
    {
    }
    
    @Override
    public void eraseUnit(String unit) throws IOException
    {
    }
    
    @Override
    public void removeUnit(String unit) throws IOException
    {
    }
    
    @Override
    public void addKey(String unit, String key, DataType type)
            throws IOException
    {
    }
    
    @Override
    public void addEntry(String unit, StorageEntry entry) throws IOException
    {
    }
    
    @Override
    public void updateEntries(
            String unit, StorageEntry entrySubset, Selector selector
    ) throws IOException
    {
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
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
    public void executeBatch() throws IOException
    {
        // Batching is not supported.
    }
    
    @Override
    public void clearBatch() throws IOException
    {
        // Batching is not supported.
    }
}
