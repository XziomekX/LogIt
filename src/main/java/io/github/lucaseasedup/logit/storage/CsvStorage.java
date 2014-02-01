/*
 * CsvStorage.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.util.LinkedHashtable;

public final class CsvStorage extends Storage
{
    public CsvStorage(File dir)
    {
        if (dir == null)
            throw new NullPointerException();
        
        this.dir = dir;
    }
    
    @Override
    public void connect() throws IOException
    {
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
    public Hashtable<String, Type> getKeys(String unit) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        Hashtable<String, Type> keys = new LinkedHashtable<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(new File(dir, unit))))
        {
            String line = br.readLine();
            
            if (line == null)
                throw new IOException("Null line.");
            
            String[] topValues = line.split(",");
            
            for (int i = 0; i < topValues.length; i++)
            {
                keys.put(unescapeValue(topValues[i]), Type.TEXT);
            }
        }
        
        return keys;
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit) throws IOException
    {
        return selectEntries(unit, null);
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        return selectEntries(unit, keys, new SelectorConstant(true));
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit,
                                                         List<String> keys,
                                                         Selector selector) throws IOException
    {
        List<Hashtable<String, String>> rs = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(new File(dir, unit))))
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
                while (!line.endsWith("\""))
                {
                    String nextLine = br.readLine();
                    
                    if (nextLine == null)
                        throw new IOException("Corrupted CSV file.");
                    
                    line += "\r\n" + nextLine;
                }
                
                String[] lineValues = line.split("(?<=\"),(?=\")");
                Hashtable<String, String> row = new LinkedHashtable<>();
                
                for (int i = 0; i < lineValues.length; i++)
                {
                    if (keys == null || keys.contains(tableKeys[i]))
                    {
                        String value = unescapeValue(lineValues[i]);
                        
                        if (value == null)
                        {
                            value = "";
                        }
                        
                        row.put(tableKeys[i], value);
                    }
                }
                
                if (resolveSelector(selector, row))
                {
                    rs.add(row);
                }
            }
        }
        
        return rs;
    }
    
    @Override
    public void createUnit(String unit, Hashtable<String, Type> keys) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
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
        
        try (BufferedReader bw = new BufferedReader(new FileReader(new File(dir, unit))))
        {
            keys = bw.readLine();
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, unit))))
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
    public void addKey(String unit, String key, Type type) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        Hashtable<String, Type> keys = getKeys(unit);
        
        if (keys.contains(key))
            throw new IOException("Key with this name already exists: " + key);
        
        List<Hashtable<String, String>> rs = selectEntries(unit);
        
        keys.put(key, type);
        removeUnit(unit);
        createUnit(unit, keys);
        
        for (Hashtable<String, String> entry : rs)
        {
            addEntry(unit, entry);
        }
    }
    
    @Override
    public void addEntry(String unit, Hashtable<String, String> pairs) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        Hashtable<String, Type> keys = getKeys(unit);
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, unit), true)))
        {
            StringBuilder sb = new StringBuilder();
            
            for (String key : keys.keySet())
            {
                if (sb.length() > 0)
                    sb.append(",");
                
                String value = pairs.get(key);
                
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
    public void updateEntries(String unit, Hashtable<String, String> pairs, Selector selector)
            throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        Hashtable<String, Type> keys = getKeys(unit);
        List<Hashtable<String, String>> rs = selectEntries(unit);
        
        removeUnit(unit);
        createUnit(unit, keys);
        
        for (Hashtable<String, String> entry : rs)
        {
            if (resolveSelector(selector, entry))
            {
                entry.putAll(pairs);
            }
            
            addEntry(unit, entry);
        }
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
        if (!connected)
            throw new IOException("Database closed.");
        
        Hashtable<String, Type> keys = getKeys(unit);
        List<Hashtable<String, String>> rs = selectEntries(unit);
        
        removeUnit(unit);
        createUnit(unit, keys);
        
        for (Hashtable<String, String> entry : rs)
        {
            if (!resolveSelector(selector, entry))
            {
                addEntry(unit, entry);
            }
        }
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
            throw new NullPointerException();
        
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
    
    private boolean resolveSelector(Selector selector, Map<String, String> entry)
    {
        if (selector instanceof SelectorConstant)
        {
            return ((SelectorConstant) selector).getValue();
        }
        else if (selector instanceof SelectorNegation)
        {
            return resolveSelector(((SelectorNegation) selector).getOperand(), entry);
        }
        else if (selector instanceof SelectorBinary)
        {
            SelectorBinary selectorBinary = (SelectorBinary) selector;
            
            switch (selectorBinary.getRelation())
            {
            case AND:
                return resolveSelector(selectorBinary.getLeftOperand(), entry)
                        && resolveSelector(selectorBinary.getRightOperand(), entry);
                
            case OR:
                return resolveSelector(selectorBinary.getLeftOperand(), entry)
                        || resolveSelector(selectorBinary.getRightOperand(), entry);
                
            default:
                throw new RuntimeException("Unsupported relation: " + selectorBinary.getRelation());
            }
        }
        else if (selector instanceof SelectorCondition)
        {
            SelectorCondition selectorCondition = (SelectorCondition) selector;
            String key = selectorCondition.getKey();
            String operandValue = selectorCondition.getValue();
            String actualValue = entry.get(key);
            
            switch (selectorCondition.getRelation())
            {
            case EQUALS:
                if (actualValue == null)
                    return operandValue == null;
                else
                    return actualValue.equals(operandValue);
                
            case LESS_THAN:
                try
                {
                    return Long.parseLong(actualValue) < Long.parseLong(operandValue);
                }
                catch (NumberFormatException ex)
                {
                    return false;
                }
                
            case GREATER_THAN:
                try
                {
                    return Long.parseLong(actualValue) > Long.parseLong(operandValue);
                }
                catch (NumberFormatException ex)
                {
                    return false;
                }
                
            case STARTS_WITH:
                return actualValue.startsWith(operandValue);
                
            case ENDS_WITH:
                return actualValue.endsWith(operandValue);
                
            case CONTAINS:
                return actualValue.contains(operandValue);
                
            default:
                throw new RuntimeException("Unsupported relation: " + selectorCondition.getRelation());
            }
        }
        else
        {
            throw new RuntimeException("Unsupported selector: " + selector.getClass().getName());
        }
    }
    
    private final File dir;
    private boolean connected = false;
}
