/*
 * Database.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author LucasEasedUp
 */
public abstract class Database implements AutoCloseable
{
    public Database()
    {
        buffer = Collections.synchronizedList(new LinkedList<String>());
    }
    
    public abstract void connect(String host, String user, String password, String database) throws SQLException;
    public abstract boolean isConnected();
    public abstract void close() throws SQLException;
    
    public abstract ResultSet executeQuery(String sql) throws SQLException;
    public abstract List<String> getColumnNames(String table) throws SQLException;
    public abstract ResultSet select(String table, String... columns) throws SQLException;
    
    public boolean executeStatement(String sql) throws SQLException
    {
        if (isBufferingEnabled())
        {
            return pushStatement(sql);
        }
        else
        {
            return executeStatementNow(sql);
        }
    }
    
    public abstract boolean createTable(String table, String... columns) throws SQLException;
    public abstract boolean createTableIfNotExists(String table, String... columns) throws SQLException;
    public abstract boolean renameTable(String table, String newTable) throws SQLException;
    public abstract boolean truncateTable(String table) throws SQLException;
    public abstract boolean dropTable(String table) throws SQLException;
    public abstract boolean insert(String table, String... values) throws SQLException;
    public abstract boolean insert(String table, String[] columns, String... values) throws SQLException;
    public abstract boolean update(String table, String[] where, String... set) throws SQLException;
    public abstract boolean delete(String table, String[] where) throws SQLException;
    
    public void toggleBuffering(boolean status)
    {
        bufferingEnabled = status;
    }
    
    public boolean isBufferingEnabled()
    {
        return bufferingEnabled;
    }
    
    public void clearBuffer()
    {
        buffer.clear();
    }
    
    public void flush() throws SQLException
    {
        if (!isBufferingEnabled())
            return;
        
        while (!buffer.isEmpty())
        {
            executeStatementNow(buffer.remove(0));
        }
    }
    
    protected abstract boolean executeStatementNow(String sql) throws SQLException;
    
    protected boolean pushStatement(String sql)
    {
        return buffer.add(sql);
    }
    
    private boolean bufferingEnabled = false;
    private List<String> buffer;
}
