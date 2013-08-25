/*
 * H2Database.java
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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author LucasEasedUp
 */
public final class H2Database extends Database
{
    public H2Database(String host)
    {
        this.host = host;
    }
     
    @Override
    public void connect() throws SQLException
    {
        org.h2.Driver.load();
        
        connection = DriverManager.getConnection(host);
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
        ResultSet tableInfo = executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS"
            + " WHERE TABLE_NAME = '" + SqlUtils.escapeQuotes(table, "'", false) + "';");
        ColumnList columnList = new ColumnList();
        
        while (tableInfo.next())
        {
            columnList.add(tableInfo.getString("COLUMN_NAME"));
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
        String sql = "SELECT " + SqlUtils.implodeColumns(columns, "\"", false)
                + " FROM \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\";";
        
        return copyResultSet(statement.executeQuery(sql));
    }
    
    @Override
    public List<Map<String, String>> select(String table, String[] columns, String[] where)
            throws SQLException
    {
        String sql = "SELECT " + SqlUtils.implodeColumns(columns, "\"", false)
                + " FROM \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " WHERE " + SqlUtils.implodeWhere(where, "\"", "'", false) + ";";
        
        return copyResultSet(statement.executeQuery(sql));
    }
    
    @Override
    public boolean createTable(String table, String[] columns) throws SQLException
    {
        String sql = "CREATE TABLE \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " (" + SqlUtils.buildColumnDefinition(columns, "\"", false) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean createTableIfNotExists(String table, String[] columns) throws SQLException
    {
        String sql = "CREATE TABLE IF NOT EXISTS \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " (" + SqlUtils.buildColumnDefinition(columns, "\"", false) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean renameTable(String table, String newTable) throws SQLException
    {
        String sql = "ALTER TABLE \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " RENAME TO \"" + SqlUtils.escapeQuotes(newTable, "\"", false) + "\";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean truncateTable(String table) throws SQLException
    {
        String sql = "TRUNCATE TABLE \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean dropTable(String table) throws SQLException
    {
        String sql = "DROP TABLE \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean addColumn(String table, String name, String type) throws SQLException
    {
        String sql = "ALTER TABLE \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " ADD COLUMN \"" + SqlUtils.escapeQuotes(name, "\"", false) + "\" " + type + ";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean insert(String table, String[] values) throws SQLException
    {
        String sql = "INSERT INTO \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " VALUES (" + SqlUtils.implodeValues(values, "'", false) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean insert(String table, String[] columns, String[] values) throws SQLException
    {
        String sql = "INSERT INTO \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " (" + SqlUtils.implodeColumns(columns, "\"", false) + ")"
                + " VALUES (" + SqlUtils.implodeValues(values, "'", false) + ");";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean update(String table, String[] where, String[] set) throws SQLException
    {
        String sql = "UPDATE \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " SET " + SqlUtils.implodeSet(set, "\"", "'", false)
                + " WHERE " + SqlUtils.implodeWhere(where, "\"", "'", false) + ";";
        
        return executeStatement(sql);
    }
    
    @Override
    public boolean delete(String table, String[] where) throws SQLException
    {
        String sql = "DELETE FROM \"" + SqlUtils.escapeQuotes(table, "\"", false) + "\""
                + " WHERE " + SqlUtils.implodeWhere(where, "\"", "'", false) + ";";
        
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
