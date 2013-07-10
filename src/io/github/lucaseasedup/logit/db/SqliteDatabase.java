/*
 * SqliteDatabase.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
package io.github.lucaseasedup.logit.db;

import io.github.lucaseasedup.logit.util.SqlUtils;
import java.sql.*;
import java.util.*;

/**
 * @author LucasEasedUp
 */
public class SqliteDatabase extends AbstractSqlDatabase
{
    public SqliteDatabase(String host)
    {
        super(host);
    }
    
    @Override
    public void connect(String user, String password, String database) throws SQLException
    {
        connection = org.sqlite.JDBC.createConnection(host, new Properties());
        statement = connection.createStatement();
    }
    
    public void connect() throws SQLException
    {
        connect(null, null, null);
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
    public Set<String> getColumnNames(String table) throws SQLException
    {
        ResultSet tableInfo = executeQuery("PRAGMA table_info('" + SqlUtils.escapeQuotes(table, "'", true) + "');");
        Set<String> columnNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        
        while (tableInfo.next())
        {
            columnNames.add(tableInfo.getString("name"));
        }
        
        return columnNames;
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        return statement.executeQuery(sql);
    }
    
    @Override
    public boolean executeStatement(String sql) throws SQLException
    {
        if (!isAutobatchEnabled())
            return statement.execute(sql);
        
        addBatch(sql);
        
        return false;
    }
    
    @Override
    public ResultSet select(String table, String[] columns) throws SQLException
    {
        return statement.executeQuery("SELECT " + SqlUtils.implodeColumnArray(columns, "`", true)
            + " FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`;");
    }
    
    @Override
    public ResultSet select(String table, String[] columns, String[] where) throws SQLException
    {
        return statement.executeQuery("SELECT " + SqlUtils.implodeColumnArray(columns, "`", true)
            + " FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " WHERE " + SqlUtils.implodeWhereArray(where, "`", "'", true) + ";");
    }
    
    @Override
    public boolean createTable(String table, String[] columns) throws SQLException
    {
        return executeStatement("CREATE TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " (" + SqlUtils.implodeColumnDefinition(columns, "`", true) + ");");
    }
    
    @Override
    public boolean createTableIfNotExists(String table, String[] columns) throws SQLException
    {
        return executeStatement("CREATE TABLE IF NOT EXISTS `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " (" + SqlUtils.implodeColumnDefinition(columns, "`", true) + ");");
    }
    
    @Override
    public boolean renameTable(String table, String newTable) throws SQLException
    {
        return executeStatement("ALTER TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " RENAME TO `" + SqlUtils.escapeQuotes(newTable, "`", true) + "`;");
    }
    
    @Override
    public boolean truncateTable(String table) throws SQLException
    {
        return executeStatement("DELETE FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`;");
    }
    
    @Override
    public boolean dropTable(String table) throws SQLException
    {
        return executeStatement("DROP TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`;");
    }
    
    @Override
    public boolean addColumn(String table, String name, String type) throws SQLException
    {
        return executeStatement("ALTER TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " ADD COLUMN `" + SqlUtils.escapeQuotes(name, "`", true) + "` " + type + ";");
    }
    
    @Override
    public boolean insert(String table, String[] values) throws SQLException
    {
        return executeStatement("INSERT INTO `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " VALUES (" + SqlUtils.implodeValueArray(values, "'", true) + ");");
    }
    
    @Override
    public boolean insert(String table, String[] columns, String[] values) throws SQLException
    {
        return executeStatement("INSERT INTO `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " (" + SqlUtils.implodeColumnArray(columns, "`", true) + ") VALUES (" + SqlUtils.implodeValueArray(values, "'", true) + ");");
    }
    
    @Override
    public boolean update(String table, String[] where, String[] set) throws SQLException
    {
        return executeStatement("UPDATE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " SET " + SqlUtils.implodeSetArray(set, "`", "'", true) + " WHERE " + SqlUtils.implodeWhereArray(where, "`", "'", true) + ";");
    }
    
    @Override
    public boolean delete(String table, String[] where) throws SQLException
    {
        return executeStatement("DELETE FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
            + " WHERE " + SqlUtils.implodeWhereArray(where, "`", "'", true) + ";");
    }
    
    @Override
    public void addBatch(String sql) throws SQLException
    {
        statement.addBatch(sql);
    }
    
    @Override
    public void executeBatch() throws SQLException
    {
        statement.executeBatch();
        statement.clearBatch();
    }
    
    @Override
    public void clearBatch() throws SQLException
    {
        statement.clearBatch();
    }
    
    private Connection connection;
    private Statement statement;
}
