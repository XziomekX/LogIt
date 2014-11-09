package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.logging.CustomLevel;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class PostgreSqlStorage implements Storage
{
    public PostgreSqlStorage(String host, String user, String password)
    {
        if (host == null || user == null || password == null)
            throw new IllegalArgumentException();
        
        if (!host.startsWith("jdbc:postgresql://"))
        {
            host = "jdbc:postgresql://" + host;
        }
        
        this.host = host;
        this.user = user;
        this.password = password;
    }
    
    @Override
    public void connect() throws IOException
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            
            connection = DriverManager.getConnection(host, user, password);
            statement = connection.createStatement();
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public boolean isConnected() throws IOException
    {
        try
        {
            return connection != null && !connection.isClosed();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void ping() throws IOException
    {
        try
        {
            statement.execute("SELECT 1");
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void close() throws IOException
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException ex)
            {
                throw new IOException(ex);
            }
            finally
            {
                connection = null;
            }
        }
        
        if (statement != null)
        {
            try
            {
                statement.close();
            }
            catch (SQLException ex)
            {
                throw new IOException(ex);
            }
            finally
            {
                statement = null;
            }
        }
    }
    
    @Override
    public List<String> getUnitNames() throws IOException
    {
        List<String> units = new LinkedList<>();
        String sql = "SELECT table_schema, table_name FROM information_schema.tables"
                   + " WHERE table_type = 'BASE TABLE'"
                   + " AND table_schema NOT IN ('pg_catalog', 'information_schema');";
        
        try (ResultSet rs = executeQuery(sql))
        {
            while (rs.next())
            {
                units.add(rs.getString("table_name"));
            }
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
        
        return units;
    }
    
    @Override
    public UnitKeys getKeys(String unit) throws IOException
    {
        UnitKeys keys = new UnitKeys();
        String sql = "SELECT COLUMN_NAME, UDT_NAME FROM INFORMATION_SCHEMA.COLUMNS"
                   + " WHERE TABLE_NAME = '" + SqlUtils.escapeQuotes(unit, "'", true) + "';";
        
        try (ResultSet tableInfo = executeQuery(sql))
        {
            while (tableInfo.next())
            {
                String name = tableInfo.getString("COLUMN_NAME");
                DataType type = SqlUtils.decodeType(tableInfo.getString("UDT_NAME"));
                
                keys.put(name, type);
            }
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
        
        return keys;
    }
    
    @Override
    public String getPrimaryKey(String unit) throws IOException
    {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE"
                   + " WHERE TABLE_NAME = '" + SqlUtils.escapeQuotes(unit, "'", true) + "';";
        
        try (ResultSet rs = executeQuery(sql))
        {
            if (rs.next())
            {
                return rs.getString("COLUMN_NAME");
            }
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
        
        return null;
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit) throws IOException
    {
        String sql = "SELECT * FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            return SqlUtils.copyResultSet(executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, Selector selector)
            throws IOException
    {
        String sql = "SELECT * FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            return SqlUtils.copyResultSet(executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public List<StorageEntry> selectEntries(String unit, List<String> keys)
            throws IOException
    {
        String sql = "SELECT " + SqlUtils.translateKeyList(keys, "\"")
                   + " FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            return SqlUtils.copyResultSet(executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public List<StorageEntry> selectEntries(
            String unit, List<String> keys, Selector selector
    ) throws IOException
    {
        String sql = "SELECT " + SqlUtils.translateKeyList(keys, "\"")
                   + " FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            return SqlUtils.copyResultSet(executeQuery(sql));
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void createUnit(String unit, UnitKeys keys, String primaryKey)
            throws IOException
    {
        String sql = "CREATE TABLE IF NOT EXISTS"
                   + " \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " (" + SqlUtils.translateKeyTypeList(keys, primaryKey, "\"") + ");";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void renameUnit(String unit, String newName) throws IOException
    {
        String sql = "ALTER TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " RENAME TO \"" + SqlUtils.escapeQuotes(newName, "\"", true) + "\";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void eraseUnit(String unit) throws IOException
    {
        String sql = "TRUNCATE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void removeUnit(String unit) throws IOException
    {
        String sql = "DROP TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void addKey(String unit, String key, DataType type)
            throws IOException
    {
        String sql = "ALTER TABLE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " ADD COLUMN \"" + SqlUtils.escapeQuotes(key, "\"", true) + "\" "
                   + SqlUtils.encodeType(type) + ";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void addEntry(String unit, StorageEntry entry)
            throws DuplicateEntryException, IOException
    {
        String sql = "INSERT INTO \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " (" + SqlUtils.translateEntryNames(entry, "\"") + ")"
                   + " VALUES (" + SqlUtils.translateEntryValues(entry, "'") + ");";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            if ("23000".equals(ex.getSQLState()))
            {
                throw new DuplicateEntryException();
            }
            else
            {
                throw new IOException(ex);
            }
        }
    }
    
    @Override
    public void updateEntries(
            String unit, StorageEntry entrySubset, Selector selector
    ) throws IOException
    {
        String sql = "UPDATE \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " SET " + SqlUtils.translateEntrySubset(entrySubset, "\"", "'")
                   + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void removeEntries(String unit, Selector selector) throws IOException
    {
        String sql = "DELETE FROM \"" + SqlUtils.escapeQuotes(unit, "\"", true) + "\""
                   + " WHERE " + SqlUtils.translateSelector(selector, "\"", "'") + ";";
        
        try
        {
            executeStatement(sql);
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public boolean isAutobatchEnabled()
    {
        return autobatch;
    }
    
    @Override
    public void setAutobatchEnabled(boolean status)
    {
        autobatch = status;
    }
    
    @Override
    public void executeBatch() throws IOException
    {
        try
        {
            statement.executeBatch();
            statement.clearBatch();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public void clearBatch() throws IOException
    {
        try
        {
            statement.clearBatch();
        }
        catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
    
    private ResultSet executeQuery(String sql) throws SQLException
    {
        LogItCore.getInstance().log(CustomLevel.INTERNAL, "(Q) " + sql);
        
        return statement.executeQuery(sql);
    }
    
    private boolean executeStatement(String sql) throws SQLException
    {
        if (!isAutobatchEnabled())
        {
            LogItCore.getInstance().log(CustomLevel.INTERNAL, "(S) " + sql);
            
            return statement.execute(sql);
        }
        
        addBatch(sql);
        
        return false;
    }
    
    private void addBatch(String sql) throws SQLException
    {
        LogItCore.getInstance().log(CustomLevel.INTERNAL, "(BS) " + sql);
        
        statement.addBatch(sql);
    }
    
    private final String host;
    private final String user;
    private final String password;
    
    private Connection connection;
    private Statement statement;
    private boolean autobatch = false;
}
