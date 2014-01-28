/*
 * H2Storage.java
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

import io.github.lucaseasedup.logit.util.SqlUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import org.apache.tools.ant.util.LinkedHashtable;

public final class H2Storage extends Storage
{
    public H2Storage(String host)
    {
        this.host = host;
    }
    
    @Override
    public void connect() throws IOException
    {
        org.h2.Driver.load();
        
        try
        {
            connection = DriverManager.getConnection(host);
            statement = connection.createStatement();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public boolean isConnected() throws IOException
    {
        try
        {
            return !connection.isClosed();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void ping() throws IOException
    {
        try
        {
            statement.execute("SELECT 1");
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void close() throws IOException
    {
        try
        {
            if (connection != null)
            {
                connection.close();
                connection = null;
            }
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public Hashtable<String, Type> getKeys(String unit) throws IOException
    {
        Hashtable<String, Type> keys = new LinkedHashtable<>();
        String sql = "SELECT COLUMN_NAME, TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TABLE_NAME = '" + SqlUtils.escapeQuotes(unit, "'", false) + "';";
        
        try (ResultSet tableInfo = executeQuery(sql))
        {
            while (tableInfo.next())
            {
                String name = tableInfo.getString("COLUMN_NAME");
                Type type = SqlUtils.decodeType(tableInfo.getString("TYPE_NAME"));
                
                keys.put(name, type);
            }
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
        
        return keys;
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit) throws IOException
    {
        String sql = "SELECT * FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            return SqlUtils.copyResultSet(statement.executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit, List<String> keys) throws IOException
    {
        String sql = "SELECT " + SqlUtils.translateKeyList(keys, "\"")
                + " FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            return SqlUtils.copyResultSet(statement.executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public List<Hashtable<String, String>> selectEntries(String unit, List<String> keys, Selector selector)
            throws IOException
    {
        String sql = "SELECT " + SqlUtils.translateKeyList(keys, "\"")
                + " FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            return SqlUtils.copyResultSet(statement.executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void createUnit(String unit, Hashtable<String, Type> keys) throws IOException
    {
        String sql = "CREATE TABLE IF NOT EXISTS \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " (" + SqlUtils.translateKeyTypeList(keys, "\"") + ");";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void renameUnit(String unit, String newName) throws IOException
    {
        String sql = "ALTER TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " RENAME TO \"" + SqlUtils.escapeQuotes(newName, "\"", true) + "\";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void eraseUnit(String unit) throws IOException
    {
        String sql = "TRUNCATE TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void removeUnit(String unit) throws IOException
    {
        String sql = "DROP TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void addKey(String unit, String key, Type type) throws IOException
    {
        String sql = "ALTER TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " ADD COLUMN \"" + SqlUtils.escapeQuotes(key, "\"", true) + "\" "
                + SqlUtils.encodeType(type) + ";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void addEntry(String unit, Hashtable<String, String> pairs) throws IOException
    {
        String sql = "INSERT INTO \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " (" + SqlUtils.translatePairNames(pairs, "\"") + ")"
                + " VALUES (" + SqlUtils.translatePairValues(pairs, "'") + ");";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void updateEntries(String unit, Hashtable<String, String> pairs, Selector selector) throws IOException
    {
        String sql = "UPDATE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " SET " + SqlUtils.translatePairs(pairs, "\"", "'")
                + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
        String sql = "DELETE FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void executeBatch() throws IOException
    {
        try
        {
            statement.executeBatch();
            statement.clearBatch();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void clearBatch() throws IOException
    {
        try
        {
            statement.clearBatch();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    private ResultSet executeQuery(String sql) throws SQLException
    {
        return statement.executeQuery(sql);
    }
    
    private boolean executeStatement(String sql) throws SQLException
    {
        if (!isAutobatchEnabled())
            return statement.execute(sql);
        
        addBatch(sql);
        
        return false;
    }
    
    private void addBatch(String sql) throws SQLException
    {
        statement.addBatch(sql);
    }
    
    private final String host;
    private Connection connection;
    private Statement statement;
}
