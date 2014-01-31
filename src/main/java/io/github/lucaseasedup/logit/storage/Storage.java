/*
 * Storage.java
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
import java.util.Hashtable;
import java.util.List;

public abstract class Storage implements AutoCloseable
{
    public abstract void connect() throws IOException;
    public abstract boolean isConnected() throws IOException;
    public abstract void ping() throws IOException;
    
    @Override
    public abstract void close() throws IOException;
    
    public abstract Hashtable<String, Type> getKeys(String unit) throws IOException;
    
    public abstract List<Hashtable<String, String>> selectEntries(String unit)
            throws IOException;
    public abstract List<Hashtable<String, String>> selectEntries(String unit,
                                                            List<String> keys) throws IOException;
    public abstract List<Hashtable<String, String>> selectEntries(String unit,
                                                            List<String> keys,
                                                            Selector selector) throws IOException;
    
    public abstract void createUnit(String unit, Hashtable<String, Type> keys)
            throws IOException;
    public abstract void renameUnit(String unit, String newName) throws IOException;
    public abstract void eraseUnit(String unit) throws IOException;
    public abstract void removeUnit(String unit) throws IOException;
    
    public abstract void addKey(String unit, String key, Type type) throws IOException;
    public abstract void addEntry(String unit, Hashtable<String, String> pairs) throws IOException;
    public abstract void updateEntries(String unit,
                                       Hashtable<String, String> pairs,
                                       Selector selector) throws IOException;
    public abstract void removeEntries(String unit, Selector selector) throws IOException;
    
    public abstract void executeBatch() throws IOException;
    public abstract void clearBatch() throws IOException;
    
    public final boolean isAutobatchEnabled()
    {
        return autobatch;
    }
    
    public final void setAutobatchEnabled(boolean status)
    {
        autobatch = status;
    }
    
    public enum Type
    {
        INTEGER, REAL,
        
        TINYTEXT,   // <= 255
        MEDIUMTEXT, // <= 1023
        LONGTEXT,   // <= 10119
        TEXT;       // Unlimited
    }
    
    private boolean autobatch = false;
}
