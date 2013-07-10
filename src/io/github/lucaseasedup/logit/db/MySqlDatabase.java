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
package io.github.lucaseasedup.logit.db;

import io.github.lucaseasedup.logit.util.SqlUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LucasEasedUp
 */
public class MySqlDatabase extends AbstractSqlDatabase
{
    public MySqlDatabase(String host)
    {
        super(host);
    }
    
    @Override
    public void connect(String user, String password, String database) throws SQLException
    {
        connection = DriverManager.getConnection(host, user, password);
        statement = connection.createStatement();
        statement.execute("USE `" + SqlUtils.escapeQuotes(database, "`") + "`;");
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
    public List<String> getColumnNames(String table) throws SQLException
    {
        ResultSet tableInfo = executeQuery("DESCRIBE `" + SqlUtils.escapeQuotes(table, "`") + "`;");
        ArrayList<String> columnNames = new ArrayList<>();
        
        while (tableInfo.next())
        {
            columnNames.add(tableInfo.getString("Field"));
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
        return statement.executeQuery("SELECT " + SqlUtils.implodeColumnArray(columns)
            + " FROM `" + SqlUtils.escapeQuotes(table, "`") + "`;");
    }
    
    @Override
    public ResultSet select(String table, String[] columns, String[] where) throws SQLException
    {
        return statement.executeQuery("SELECT " + SqlUtils.implodeColumnArray(columns)
            + " FROM `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " WHERE " + SqlUtils.implodeWhereArray(where) + ";");
    }
    
    @Override
    public boolean createTable(String table, String[] columns) throws SQLException
    {
        return executeStatement("CREATE TABLE `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " (" + SqlUtils.implodeColumnDefinition(columns) + ");");
    }
    
    @Override
    public boolean createTableIfNotExists(String table, String[] columns) throws SQLException
    {
        return executeStatement("CREATE TABLE IF NOT EXISTS `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " (" + SqlUtils.implodeColumnDefinition(columns) + ");");
    }
    
    @Override
    public boolean renameTable(String table, String newTable) throws SQLException
    {
        return executeStatement("ALTER TABLE `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " RENAME TO `" + SqlUtils.escapeQuotes(newTable, "`") + "`;");
    }
    
    @Override
    public boolean truncateTable(String table) throws SQLException
    {
        return executeStatement("TRUNCATE TABLE `" + SqlUtils.escapeQuotes(table, "`") + "`;");
    }
    
    @Override
    public boolean dropTable(String table) throws SQLException
    {
        return executeStatement("DROP TABLE `" + SqlUtils.escapeQuotes(table, "`") + "`;");
    }
    
    @Override
    public boolean addColumn(String table, String name, String type) throws SQLException
    {
        return executeStatement("ALTER TABLE `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " ADD COLUMN `" + SqlUtils.escapeQuotes(name, "`") + "` " + type + ";");
    }
    
    @Override
    public boolean insert(String table, String[] values) throws SQLException
    {
        return executeStatement("INSERT INTO `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " VALUES (" + SqlUtils.implodeValueArray(values) + ");");
    }
    
    @Override
    public boolean insert(String table, String[] columns, String[] values) throws SQLException
    {
        return executeStatement("INSERT INTO `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " (" + SqlUtils.implodeColumnArray(columns) + ") VALUES (" + SqlUtils.implodeValueArray(values) + ");");
    }
    
    @Override
    public boolean update(String table, String[] where, String[] set) throws SQLException
    {
        return executeStatement("UPDATE `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " SET " + SqlUtils.implodeSetArray(set) + " WHERE " + SqlUtils.implodeWhereArray(where) + ";");
    }
    
    @Override
    public boolean delete(String table, String[] where) throws SQLException
    {
        return executeStatement("DELETE FROM `" + SqlUtils.escapeQuotes(table, "`") + "`"
            + " WHERE " + SqlUtils.implodeWhereArray(where) + ";");
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
