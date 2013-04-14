/*
 * MySqlDatabase.java
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

import com.gmail.lucaseasedup.logit.util.ArrayUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LucasEasedUp
 */
public class MySqlDatabase extends Database
{
    @Override
    public void connect(String host, String user, String password, String database) throws SQLException
    {
        connection = DriverManager.getConnection(host, user, password);
        statement = connection.createStatement();
        statement.execute("USE " + database + ";");
    }
    
    @Override
    public boolean isConnected()
    {
        try
        {
            return !connection.isClosed();
        }
        catch (SQLException ex)
        {
            return false;
        }
    }
    
    @Override
    public void close() throws SQLException
    {
        if (connection != null)
        {
            connection.close();
            connection = null;
        }
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        return statement.executeQuery(sql);
    }
    
    @Override
    public List<String> getColumnNames(String table) throws SQLException
    {
        ResultSet tableInfo = executeQuery("PRAGMA table_info('" + table + "');");
        ArrayList<String> columnNames = new ArrayList<>();
        
        while (tableInfo.next())
        {
            columnNames.add(tableInfo.getString("name"));
        }
        
        return columnNames;
    }
    
    @Override
    public ResultSet select(String table, String... columns) throws SQLException
    {
        return statement.executeQuery("SELECT " + ArrayUtils.implodeArray(columns, ",") + " FROM " + table + ";");
    }
    
    @Override
    public boolean createTable(String table, String... columns) throws SQLException
    {
        return executeStatement("CREATE TABLE " + table + " (" + ArrayUtils.implodeArray(columns, ",") + ");");
    }
    
    @Override
    public boolean createTableIfNotExists(String table, String... columns) throws SQLException
    {
        return executeStatement("CREATE TABLE IF NOT EXISTS " + table + " (" + ArrayUtils.implodeArray(columns, ",") + ");");
    }
    
    @Override
    public boolean renameTable(String table, String newTable) throws SQLException
    {
        return executeStatement("ALTER TABLE " + table + " RENAME TO " + newTable + ";");
    }
    
    @Override
    public boolean truncateTable(String table) throws SQLException
    {
        return executeStatement("TRUNCATE TABLE " + table + ";");
    }
    
    @Override
    public boolean dropTable(String table) throws SQLException
    {
        return executeStatement("DROP TABLE " + table + ";");
    }
    
    @Override
    public boolean insert(String table, String... values) throws SQLException
    {
        return executeStatement("INSERT INTO " + table + " VALUES (" + ArrayUtils.implodeArray(values, ",", "\"", "\"") + ");");
    }
    
    @Override
    public boolean insert(String table, String[] columns, String... values) throws SQLException
    {
        return executeStatement("INSERT INTO " + table + " (" + ArrayUtils.implodeArray(columns, ",") + ") VALUES (" + ArrayUtils.implodeArray(values, ",", "\"", "\"") + ");");
    }
    
    @Override
    public boolean update(String table, String[] where, String... set) throws SQLException
    {
        return executeStatement("UPDATE " + table + " SET " + ArrayUtils.implodeKeyValueArray(set, ",", "=", "\"", "\"") + " WHERE " + ArrayUtils.implodeKeyValueArray(where, " AND ", "=", "\"", "\"") + ";");
    }
    
    @Override
    public boolean delete(String table, String[] where) throws SQLException
    {
        return executeStatement("DELETE FROM " + table + " WHERE " + ArrayUtils.implodeKeyValueArray(where, " AND ", "=", "\"", "\"") + ";");
    }
    
    @Override
    protected boolean executeStatementNow(String sql) throws SQLException
    {
        return statement.execute(sql);
    }
    
    private Connection connection;
    private Statement statement;
}
