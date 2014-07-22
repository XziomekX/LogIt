/*
 * NullStorage.java
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
