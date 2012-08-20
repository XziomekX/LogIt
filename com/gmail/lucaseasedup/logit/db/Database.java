package com.gmail.lucaseasedup.logit.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author LucasEasedUp
 */
public interface Database
{
    public void connect(String host, String user, String password, String database) throws SQLException;
    public boolean executeStatement(String sql) throws SQLException;
    public ResultSet executeQuery(String sql) throws SQLException;
    public boolean create(String table, String columns) throws SQLException;
    public ResultSet select(String table, String columns) throws SQLException;
    public boolean insert(String table, String values) throws SQLException;
    public boolean update(String table, String set, String where) throws SQLException;
    public boolean delete(String table, String where) throws SQLException;
    public boolean truncate(String table) throws SQLException;
    public void close() throws SQLException;
}
