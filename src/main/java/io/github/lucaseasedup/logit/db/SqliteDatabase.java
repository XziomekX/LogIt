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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class SqliteDatabase extends Database
{
    public SqliteDatabase(String host)
    {
        this.host = host;
    }
    
    @Override
    public void connect() throws SQLException
    {
        connection = org.sqlite.JDBC.createConnection(host, new Properties());
        statement = connection.createStatement();
    }
    
    @Override
    public boolean isConnected() throws SQLException
    {
        return !connection.isClosed();
    }
    
    @Override
    public void ping() throws SQLException
    {
        statement.execute("SELECT 1");
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
    public ColumnList getColumnNames(String table) throws SQLException
    {
        ResultSet tableInfo =
                executeQuery("PRAGMA table_info('" + SqlUtils.escapeQuotes(table, "'", true) + "');");
        ColumnList columnList = new ColumnList();
        
        while (tableInfo.next())
        {
            columnList.add(tableInfo.getString("name"));
        }
        
        return columnList;
    }
    
    public ResultSet executeQuery(String sql) throws SQLException
    {
        return statement.executeQuery(sql);
    }
    
    public boolean executeStatement(String sql) throws SQLException
    {
        if (!isAutobatchEnabled())
            return statement.execute(sql);
        
        addBatch(sql);
        
        return false;
    }
    
    @Override
    public List<Map<String, String>> select(String table, String[] columns) throws SQLException
    {
        String sql = "SELECT " + SqlUtils.implodeColumns(columns, "`", true)
                + " FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`;";
        
        return copyResultSet(statement.executeQuery(sql));
    }
    
    @Override
    public List<Map<String, String>> select(String table, String[] columns, String[] where)
            throws SQLException
    {
        String sql = "SELECT " + SqlUtils.implodeColumns(columns, "`", true)
                + " FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " WHERE " + SqlUtils.implodeWhere(where, "`", "'", true) + ";";
        
        return copyResultSet(statement.executeQuery(sql));
    }
    
    @Override
    public boolean createTable(String table, String[] columns) throws SQLException
    {
        String sql = "CREATE TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " (" + SqlUtils.buildColumnDefinition(columns, "`", true) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean createTableIfNotExists(String table, String[] columns) throws SQLException
    {
        String sql = "CREATE TABLE IF NOT EXISTS `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " (" + SqlUtils.buildColumnDefinition(columns, "`", true) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean renameTable(String table, String newTable) throws SQLException
    {
        String sql = "ALTER TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " RENAME TO `" + SqlUtils.escapeQuotes(newTable, "`", true) + "`;";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean truncateTable(String table) throws SQLException
    {
        String sql = "DELETE FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`;";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean dropTable(String table) throws SQLException
    {
        String sql = "DROP TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`;";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean addColumn(String table, String name, String type) throws SQLException
    {
        String sql = "ALTER TABLE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " ADD COLUMN `" + SqlUtils.escapeQuotes(name, "`", true) + "` " + type + ";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean insert(String table, String[] values) throws SQLException
    {
        String sql = "INSERT INTO `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " VALUES (" + SqlUtils.implodeValues(values, "'", true) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean insert(String table, String[] columns, String[] values) throws SQLException
    {
        String sql = "INSERT INTO `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " (" + SqlUtils.implodeColumns(columns, "`", true) + ")"
                + " VALUES (" + SqlUtils.implodeValues(values, "'", true) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean update(String table, String[] where, String[] set) throws SQLException
    {
        String sql = "UPDATE `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " SET " + SqlUtils.implodeSet(set, "`", "'", true)
                + " WHERE " + SqlUtils.implodeWhere(where, "`", "'", true) + ";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean delete(String table, String[] where) throws SQLException
    {
        String sql = "DELETE FROM `" + SqlUtils.escapeQuotes(table, "`", true) + "`"
                + " WHERE " + SqlUtils.implodeWhere(where, "`", "'", true) + ";";
        
        return executeStatement(sql);
    }
    
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
    
    private final String host;
    private Connection connection;
    private Statement statement;
}
