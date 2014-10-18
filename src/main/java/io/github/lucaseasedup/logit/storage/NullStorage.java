package io.github.lucaseasedup.logit.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public final class NullStorage extends Storage
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
    public Hashtable<String, DataType> getKeys(String unit) throws IOException
    {
        return new Hashtable<>();
    }
    
    @Override
    public String getPrimaryKey(String unit) throws IOException
    {
        return null;
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit) throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit, List<String> keys) throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit, Selector selector) throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public List<Storage.Entry> selectEntries(String unit, List<String> keys, Selector selector)
            throws IOException
    {
        return new ArrayList<>();
    }
    
    @Override
    public void createUnit(String unit, Hashtable<String, DataType> keys, String primaryKey)
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
    public void addKey(String unit, String key, DataType type) throws IOException
    {
    }
    
    @Override
    public void addEntry(String unit, Storage.Entry entry) throws IOException
    {
    }
    
    @Override
    public void updateEntries(String unit, Storage.Entry entrySubset, Selector selector)
            throws IOException
    {
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
    }
    
    @Override
    public void executeBatch() throws IOException
    {
    }
    
    @Override
    public void clearBatch() throws IOException
    {
    }
}
