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

import java.sql.*;

/**
 * @author LucasEasedUp
 */
public class SqliteDatabase implements Database
{
    @Override
    public void connect(String host, String user, String password, String database) throws SQLException
    {
        connection = DriverManager.getConnection(host);
        statement = connection.createStatement();
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
    public boolean create(String table, String columns) throws SQLException
    {
        return statement.execute("CREATE TABLE IF NOT EXISTS " + table + " (" + columns + ");");
    }
    
    @Override
    public ResultSet select(String table, String columns) throws SQLException
    {
        return statement.executeQuery("SELECT " + columns + " FROM " + table + ";");
    }
    
    @Override
    public boolean insert(String table, String values) throws SQLException
    {
        return statement.execute("INSERT INTO " + table + " VALUES (" + values + ");");
    }
    
    @Override
    public boolean update(String table, String set, String where) throws SQLException
    {
        return statement.execute("UPDATE " + table + " SET " + set + " WHERE " + where + ";");
    }
    
    @Override
    public boolean delete(String table, String where) throws SQLException
    {
        return statement.execute("DELETE FROM " + table + " WHERE " + where + ";");
    }
    
    @Override
    public boolean truncate(String table) throws SQLException
    {
        return statement.execute("DELETE FROM " + table + ";");
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
