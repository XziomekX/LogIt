/*
 * AbstractRelationalDatabase.java
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author LucasEasedUp
 */
public abstract class AbstractRelationalDatabase implements AutoCloseable
{
    public AbstractRelationalDatabase(String host)
    {
        this.host = host;
    }
    
    public abstract void connect() throws IOException, SQLException, ReflectiveOperationException;
    public abstract boolean isConnected();
    public abstract void ping() throws SQLException;
    
    @Override
    public abstract void close() throws SQLException;
    
    public abstract ArrayList<String> getColumnNames(String table) throws SQLException;
    public abstract ResultSet executeQuery(String sql) throws SQLException;
    public abstract boolean executeStatement(String sql) throws SQLException;
    
    public abstract ResultSet select(String table, String[] columns) throws SQLException;
    public abstract ResultSet select(String table, String[] columns, String[] where) throws SQLException;
    
    public abstract boolean createTable(String table, String[] columns) throws SQLException;
    public abstract boolean createTableIfNotExists(String table, String[] columns) throws SQLException;
    public abstract boolean renameTable(String table, String newTable) throws SQLException;
    public abstract boolean truncateTable(String table) throws SQLException;
    public abstract boolean dropTable(String table) throws SQLException;
    public abstract boolean addColumn(String table, String name, String type) throws SQLException;
    public abstract boolean insert(String table, String[] values) throws SQLException;
    public abstract boolean insert(String table, String[] columns, String[] values) throws SQLException;
    public abstract boolean update(String table, String[] where, String[] set) throws SQLException;
    public abstract boolean delete(String table, String[] where) throws SQLException;
    
    public abstract void addBatch(String sql) throws SQLException;
    public abstract void executeBatch() throws SQLException;
    public abstract void clearBatch() throws SQLException;
    
    public boolean isAutobatchEnabled()
    {
        return autobatch;
    }
    
    public void setAutobatchEnabled(boolean status)
    {
        autobatch = status;
    }
    
    protected boolean autobatch = false;
    protected final String host;
}
