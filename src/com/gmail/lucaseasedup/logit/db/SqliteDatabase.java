/*
 * SqliteDatabase.java
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

/**
 * @author LucasEasedUp
 */
public class SqliteDatabase implements Database, AutoCloseable
{
    @Override
    public void connect(String host, String user, String password, String database) throws SQLException
    {
        connection = DriverManager.getConnection(host);
        statement = connection.createStatement();
    }
    
    public void connect(String host) throws SQLException
    {
        connect(host, null, null, null);
    }
    
    @Override
    public boolean executeStatement(String sql) throws SQLException
    {
        return statement.execute(sql);
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        return statement.executeQuery(sql);
    }
    
    @Override
    public boolean create(String table, String... columns) throws SQLException
    {
        return statement.execute("CREATE TABLE IF NOT EXISTS " + table + " (" + ArrayUtils.implodeArray(columns, ",") + ");");
    }
    
    @Override
    public ResultSet select(String table, String... columns) throws SQLException
    {
        return statement.executeQuery("SELECT " + ArrayUtils.implodeArray(columns, ",") + " FROM " + table + ";");
    }
    
    @Override
    public boolean insert(String table, String... values) throws SQLException
    {
        return statement.execute("INSERT INTO " + table + " VALUES (" + ArrayUtils.implodeArray(values, ",", "\"", "\"") + ");");
    }
    
    @Override
    public boolean update(String table, String[] where, String... set) throws SQLException
    {
        return statement.execute("UPDATE " + table + " SET " + ArrayUtils.implodeKeyValueArray(set, ",", "=", "\"", "\"") + " WHERE " + ArrayUtils.implodeKeyValueArray(where, " AND ", "=", "\"", "\"") + ";");
    }
    
    @Override
    public boolean delete(String table, String[] where) throws SQLException
    {
        return statement.execute("DELETE FROM " + table + " WHERE " + ArrayUtils.implodeKeyValueArray(where, " AND ", "=", "\"", "\"") + ";");
    }
    
    @Override
    public boolean truncate(String table) throws SQLException
    {
        return statement.execute("DELETE FROM " + table + ";");
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
    
    private Connection connection;
    private Statement statement;
}
