package com.gmail.lucaseasedup.logit.db;

import java.sql.*;

/**
 * @author LucasEasedUp
 */
public class MySqlDatabase implements Database
{
    @Override
    public void connect(String host, String user, String password, String database) throws SQLException
    {
        connection = DriverManager.getConnection(host, user, password);
        statement = connection.createStatement();
        statement.execute("USE " + database + ";");
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
        return statement.execute("TRUNCATE TABLE " + table + ";");
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
