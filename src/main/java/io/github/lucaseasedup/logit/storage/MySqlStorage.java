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

public final class MySqlStorage implements Storage
{
    public MySqlStorage(
            String host, String user, String password, String database
    )
    {
        if (host == null || user == null
                || password == null || database == null)
        {
            throw new IllegalArgumentException();
        }
        
        if (!host.startsWith("jdbc:mysql://"))
        {
            host = "jdbc:mysql://" + host;
        }
        
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
    }
    
    @Override
    public void connect() throws IOException
    {
        try
        {
            connection = DriverManager.getConnection(host, user, password);
            statement = connection.createStatement();
            statement.execute(
                    "USE `" + SqlUtils.escapeQuotes(database, "`", true) + "`;"
            );
        }
        catch (SQLException ex)
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
        String sql = "SHOW TABLES;";
        
        try (ResultSet rs = executeQuery(sql))
        {
            while (rs.next())
            {
                units.add(rs.getString(1));
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
        String sql = "DESCRIBE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`;";
        
        try (ResultSet tableInfo = executeQuery(sql))
        {
            while (tableInfo.next())
            {
                String name = tableInfo.getString("Field");
                DataType type = SqlUtils.decodeType(
                        tableInfo.getString("Type")
                );
                
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
        String sql = "DESCRIBE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`;";
        
        try (ResultSet rs = executeQuery(sql))
        {
            while (rs.next())
            {
                if ("PRI".equals(rs.getString("Key")))
                {
                    return rs.getString("Field");
                }
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
        String sql = "SELECT * FROM `" + SqlUtils.escapeQuotes(unit, "`", true) + "`;";
        
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
        String sql = "SELECT * FROM `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " WHERE " + SqlUtils.translateSelector(selector, "`", "'") + ";";
        
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
        String sql = "SELECT " + SqlUtils.translateKeyList(keys, "`")
                   + " FROM `" + SqlUtils.escapeQuotes(unit, "`", true) + "`;";
        
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
        String sql = "SELECT " + SqlUtils.translateKeyList(keys, "`")
                   + " FROM `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " WHERE " + SqlUtils.translateSelector(selector, "`", "'") + ";";
        
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
        String sql = "CREATE TABLE IF NOT EXISTS `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " (" + SqlUtils.translateKeyTypeList(keys, primaryKey, "`") + ");";
        
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
        String sql = "ALTER TABLE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " RENAME TO `" + SqlUtils.escapeQuotes(newName, "`", true) + "`;";
        
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
        String sql = "TRUNCATE TABLE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`;";
        
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
        String sql = "DROP TABLE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`;";
        
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
        String sql = "ALTER TABLE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " ADD COLUMN `" + SqlUtils.escapeQuotes(key, "`", true) + "` "
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
        String sql = "INSERT INTO `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " (" + SqlUtils.translateEntryNames(entry, "`") + ")"
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
    )
            throws IOException
    {
        String sql = "UPDATE `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " SET " + SqlUtils.translateEntrySubset(entrySubset, "`", "'")
                   + " WHERE " + SqlUtils.translateSelector(selector, "`", "'") + ";";
        
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
        String sql = "DELETE FROM `" + SqlUtils.escapeQuotes(unit, "`", true) + "`"
                   + " WHERE " + SqlUtils.translateSelector(selector, "`", "'") + ";";
        
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
    private final String database;
    
    private Connection connection;
    private Statement statement;
    private boolean autobatch = false;
}
