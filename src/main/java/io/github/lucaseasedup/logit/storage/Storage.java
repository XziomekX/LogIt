package io.github.lucaseasedup.logit.storage;

import java.io.IOException;
import java.util.List;

public interface Storage extends AutoCloseable
{
    public void connect()
            throws IOException;
    public boolean isConnected()
            throws IOException;
    public void ping()
            throws IOException;
    @Override public void close()
            throws IOException;
    
    public List<String> getUnitNames()
            throws IOException;
    public UnitKeys getKeys(String unit)
            throws IOException;
    public String getPrimaryKey(String unit)
            throws IOException;
    
    public List<StorageEntry> selectEntries(String unit)
            throws IOException;
    public List<StorageEntry> selectEntries(String unit, Selector selector)
            throws IOException;
    public List<StorageEntry> selectEntries(String unit, List<String> keys)
            throws IOException;
    public List<StorageEntry> selectEntries(
            String unit, List<String> keys, Selector selector
    ) throws IOException;
    
    public void createUnit(String unit, UnitKeys keys, String primaryKey)
            throws IOException;
    public void renameUnit(String unit, String newName)
            throws IOException;
    public void eraseUnit(String unit)
            throws IOException;
    public void removeUnit(String unit)
            throws IOException;
    
    public void addKey(String unit, String key, DataType type)
            throws IOException;
    public void addEntry(String unit, StorageEntry entry)
            throws DuplicateEntryException, IOException;
    public void updateEntries(
            String unit, StorageEntry entrySubset, Selector selector
    ) throws IOException;
    public void removeEntries(String unit, Selector selector)
            throws IOException;
    
    public boolean isAutobatchEnabled();
    public void setAutobatchEnabled(boolean status);
    public void executeBatch()
            throws IOException;
    public void clearBatch()
            throws IOException;
}
