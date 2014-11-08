package io.github.lucaseasedup.logit.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class CsvStorage implements Storage
{
    public CsvStorage(File dir)
    {
        if (dir == null)
            throw new IllegalArgumentException();
        
        this.dir = dir;
    }
    
    @Override
    public void connect() throws IOException
    {
        if (!dir.isDirectory())
        {
            throw new IOException(
                    "CSV path is not a directory: " + dir
            );
        }
        
        connected = true;
    }
    
    @Override
    public boolean isConnected() throws IOException
    {
        return connected;
    }
    
    @Override
    public void ping() throws IOException
    {
        if (!dir.isDirectory())
        {
            throw new IOException(
                    "CSV path is not a directory: " + dir
            );
        }
    }
    
    @Override
    public void close() throws IOException
    {
        connected = false;
    }
    
    @Override
    public List<String> getUnitNames() throws IOException
    {
        File[] files = dir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isFile();
            }
        });
        
        List<String> units = new LinkedList<>();
        
        for (File file : files)
        {
            units.add(file.getName());
        }
        
        return units;
    }
    
    @Override
    public UnitKeys getKeys(String unit) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        UnitKeys keys = new UnitKeys();
        
        try (
                FileReader fr = new FileReader(new File(dir, unit));
                BufferedReader br = new BufferedReader(fr);
        )
        {
            String line = br.readLine();
            
            if (line == null)
                throw new IOException("Null line.");
            
            String[] topValues = line.split(",");
            
            for (String topValue : topValues)
            {
                keys.put(unescapeValue(topValue), DataType.TEXT);
            }
        }
        
        return keys;
    }
    
    @Override
    public String getPrimaryKey(String unit) throws IOException
    {
        return null;
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit) throws IOException
    {
        return selectEntries(unit, null, new SelectorConstant(true));
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, Selector selector)
            throws IOException
    {
        return selectEntries(unit, null, selector);
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        return selectEntries(unit, keys, new SelectorConstant(true));
    }
    
    @Override
    public List<StorageEntry> selectEntries(
            String unit, List<String> keys, Selector selector
    ) throws IOException
    {
        List<StorageEntry> entries = new ArrayList<>();
        
        try (
                FileReader fr = new FileReader(new File(dir, unit));
                BufferedReader br = new BufferedReader(fr);
        )
        {
            String line = br.readLine();
            
            if (line == null)
                throw new IOException("Null line.");
            
            String[] tableKeys = line.split(",");
            
            for (int i = 0; i < tableKeys.length; i++)
            {
                tableKeys[i] = unescapeValue(tableKeys[i]);
            }
            
            while ((line = br.readLine()) != null)
            {
                StringBuilder lineBuilder = new StringBuilder(line);
                
                while (!line.endsWith("\""))
                {
                    line = br.readLine();
                    
                    if (line == null)
                        throw new IOException("Corrupted CSV file");
                    
                    lineBuilder.append("\r\n");
                    lineBuilder.append(line);
                }
                
                String[] lineValues =
                        lineBuilder.toString().split("(?<=\"),(?=\")");
                StorageEntry.Builder entryBuilder =
                        new StorageEntry.Builder();
                
                for (int i = 0; i < lineValues.length; i++)
                {
                    if (keys == null || keys.contains(tableKeys[i]))
                    {
                        entryBuilder.put(tableKeys[i],
                                unescapeValue(lineValues[i]));
                    }
                }
                
                StorageEntry entry = entryBuilder.build();
                
                if (SqlUtils.resolveSelector(selector, entry))
                {
                    entries.add(entry);
                }
            }
        }
        
        return entries;
    }
    
    @Override
    public void createUnit(String unit, UnitKeys keys, String primaryKey)
            throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        if (primaryKey != null && !keys.containsKey(primaryKey))
        {
            throw new IllegalArgumentException(
                    "Cannot create index on a non-existing key"
            );
        }
        
        File file = new File(dir, unit);
        
        if (file.exists())
            return;
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file)))
        {
            StringBuilder sb = new StringBuilder();
            
            for (String key : keys.keySet())
            {
                if (sb.length() > 0)
                    sb.append(",");
                
                sb.append(escapeValue(key));
            }
            
            sb.append("\r\n");
            bw.write(sb.toString());
        }
    }
    
    @Override
    public void renameUnit(String unit, String newName) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        new File(dir, unit).renameTo(new File(dir, newName));
    }
    
    @Override
    public void eraseUnit(String unit) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        String keys;
        
        try (
                FileReader fr = new FileReader(new File(dir, unit));
                BufferedReader br = new BufferedReader(fr);
        )
        {
            keys = br.readLine();
        }
        
        try (
                FileWriter fw = new FileWriter(new File(dir, unit));
                BufferedWriter bw = new BufferedWriter(fw);
        )
        {
            bw.write(keys + "\r\n");
        }
    }
    
    @Override
    public void removeUnit(String unit) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        new File(dir, unit).delete();
    }
    
    @Override
    public void addKey(String unit, String key, DataType type)
            throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        UnitKeys keys = getKeys(unit);
        String primaryKey = getPrimaryKey(unit);
        
        if (keys.containsKey(key))
            throw new IOException("Key with this name already exists: " + key);
        
        List<StorageEntry> entries = selectEntries(unit);
        
        keys.put(key, type);
        removeUnit(unit);
        createUnit(unit, keys, primaryKey);
        
        for (StorageEntry entry : entries)
        {
            addEntry(unit, entry);
        }
    }
    
    @Override
    public void addEntry(String unit, StorageEntry entry) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        UnitKeys keys = getKeys(unit);
        
        try (
                FileWriter fw = new FileWriter(new File(dir, unit), true);
                BufferedWriter bw = new BufferedWriter(fw);
        )
        {
            StringBuilder sb = new StringBuilder();
            
            for (String key : keys.keySet())
            {
                if (sb.length() > 0)
                    sb.append(",");
                
                String value = entry.get(key);
                
                if (value != null && !value.isEmpty())
                {
                    sb.append(escapeValue(value));
                }
                else
                {
                    sb.append("\"\"");
                }
            }
            
            sb.append("\r\n");
            bw.write(sb.toString());
        }
    }
    
    @Override
    public void updateEntries(
            String unit, StorageEntry entrySubset, Selector selector
    ) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        UnitKeys keys = getKeys(unit);
        String primaryKey = getPrimaryKey(unit);
        
        List<StorageEntry> entries = selectEntries(unit);
        
        removeUnit(unit);
        createUnit(unit, keys, primaryKey);
        
        for (StorageEntry entry : entries)
        {
            if (SqlUtils.resolveSelector(selector, entry))
            {
                for (StorageDatum datum : entrySubset)
                {
                    entry.put(datum.getKey(), datum.getValue());
                }
            }
            
            addEntry(unit, entry);
        }
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        UnitKeys keys = getKeys(unit);
        String primaryKey = getPrimaryKey(unit);
        
        List<StorageEntry> entries = selectEntries(unit);
        
        removeUnit(unit);
        createUnit(unit, keys, primaryKey);
        
        for (StorageEntry entry : entries)
        {
            if (!SqlUtils.resolveSelector(selector, entry))
            {
                addEntry(unit, entry);
            }
        }
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
    
    private String escapeValue(String s)
    {
        s = s.replace(",", "\\,");
        
        return "\"" + s + "\"";
    }
    
    private String unescapeValue(String s)
    {
        if (s == null)
            throw new IllegalArgumentException();
        
        s = s.trim();
        s = s.replace("\\,", ",");
        
        if (s.startsWith("\""))
        {
            s = s.substring(1);
        }
        
        if (s.endsWith("\""))
        {
            s = s.substring(0, s.length() - 1);
        }
        
        return s;
    }
    
    private final File dir;
    private boolean connected = false;
}
