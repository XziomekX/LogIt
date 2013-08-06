/*
 * Table.java
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

import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author LucasEasedUp
 */
public class Table
{
    public Table(Database database, String tableName, ConfigurationSection columnsSection)
    {
        this.database = database;
        this.table = tableName;
        
        for (String s : columnsSection.getKeys(false))
        {
            ConfigurationSection column = columnsSection.getConfigurationSection(s);
            
            tableColumns.put(column.getString("id"),
                    new Column(column.getString("name"), column.getString("type"), column.getBoolean("disabled")));
        }
    }
    
    /**
     * Creates the table if it does not exists
     * and adds columns that are enabled but missing.
     * 
     * @throws SQLException Thrown on SQL error.
     */
    public void open() throws SQLException
    {
        List<String> tableDefinition = new ArrayList<>();
        
        for (Column column : tableColumns.values())
        {
            if (column.isDisabled())
                continue;
            
            tableDefinition.add(column.getName());
            tableDefinition.add(column.getType());
        }
        
        database.createTableIfNotExists(table, tableDefinition.toArray(new String[tableDefinition.size()]));
        
        ArrayList<String> existingColumns = database.getColumnNames(table);
        
        database.setAutobatchEnabled(true);
        
        for (Column column : tableColumns.values())
        {
            if (existingColumns.contains(column.getName()))
                continue;
            
            if (!column.isDisabled())
            {
                database.addColumn(table, column.getName(), column.getType());
            }
        }
        
        database.executeBatch();
        database.setAutobatchEnabled(false);
    }
    
    public List<Map<String, String>> select(String[] columns, WhereClause[] where) throws SQLException
    {
        Set<String> filteredColumnIds = new HashSet<>();
        List<String> filteredColumnNames = new ArrayList<>();
        
        for (int i = 0; i < columns.length; i++)
        {
            if (!tableColumns.containsKey(columns[i]))
                continue;
            
            if (!tableColumns.get(columns[i]).isDisabled())
            {
                filteredColumnIds.add(columns[i]);
                filteredColumnNames.add(tableColumns.get(columns[i]).getName());
            }
        }
        
        return copyResultSet(database.select(table, filteredColumnNames.toArray(new String[filteredColumnNames.size()]), convertWhereClauses(where)), filteredColumnIds);
    }
    
    public List<Map<String, String>> select(String[] columns) throws SQLException
    {
        Set<String> filteredColumnIds = new HashSet<>();
        List<String> filteredColumnNames = new ArrayList<>();
        
        for (int i = 0; i < columns.length; i++)
        {
            if (!tableColumns.containsKey(columns[i]))
                continue;
            
            if (!tableColumns.get(columns[i]).isDisabled())
            {
                filteredColumnIds.add(columns[i]);
                filteredColumnNames.add(tableColumns.get(columns[i]).getName());
            }
        }
        
        return copyResultSet(database.select(table, filteredColumnNames.toArray(new String[filteredColumnNames.size()])),
                filteredColumnIds);
    }
    
    public List<Map<String, String>> select() throws SQLException
    {
        Set<String> columns = tableColumns.keySet();
        
        return select(columns.toArray(new String[columns.size()]));
    }
    
    public void insert(String[] columns, String[] values) throws SQLException
    {
        List<String> filteredColumns = new ArrayList<>();
        List<String> filteredValues = new ArrayList<>();
        
        for (int i = 0; i < columns.length; i++)
        {
            if (!tableColumns.containsKey(columns[i]))
                continue;
            
            if (!tableColumns.get(columns[i]).isDisabled())
            {
                filteredColumns.add(tableColumns.get(columns[i]).getName());
                filteredValues.add(values[i]);
            }
        }
        
        database.insert(table, filteredColumns.toArray(new String[filteredColumns.size()]),
                filteredValues.toArray(new String[filteredValues.size()]));
    }
    
    public void delete(WhereClause[] where) throws SQLException
    {
        database.delete(table, convertWhereClauses(where));
    }
    
    public void update(WhereClause[] where, SetClause[] set) throws SQLException
    {
        String[] convertedSet = convertSetClauses(set);
        
        if (convertedSet.length == 0)
            return;
        
        database.update(table, convertWhereClauses(where), convertedSet);
    }
    
    public void truncate() throws SQLException
    {
        database.truncateTable(table);
    }
    
    public Map<String, Column> getColumns()
    {
        return tableColumns;
    }
    
    public Map<String, Column> getEnabledColumns()
    {
        Map<String, Column> enabledColumns = new HashMap<>();
        
        for (Entry<String, Column> e : tableColumns.entrySet())
        {
            if (!e.getValue().isDisabled())
            {
                enabledColumns.put(e.getKey(), e.getValue());
            }
        }
        
        return enabledColumns;
    }
    
    public String getColumnName(String id)
    {
        return tableColumns.get(id).getName();
    }
    
    public String getColumnId(String name)
    {
        for (Entry<String, Column> e : tableColumns.entrySet())
        {
            if (e.getValue().getName().equalsIgnoreCase(name))
            {
                return e.getKey();
            }
        }
        
        return null;
    }
    
    public boolean isColumnDisabled(String id)
    {
        return tableColumns.get(id).isDisabled();
    }
    
    public String getTableName()
    {
        return table;
    }
    
    private String[] convertSetClauses(SetClause[] set)
    {
        List<String> output = new ArrayList<>();
        
        for (int i = 0; i < set.length; i++)
        {
            if (tableColumns.containsKey(set[i].getColumnId())
                    && !tableColumns.get(set[i].getColumnId()).isDisabled())
            {
                output.add(tableColumns.get(set[i].getColumnId()).getName());
                output.add(set[i].getValue());
            }
        }
        
        return output.toArray(new String[output.size()]);
    }
    
    private String[] convertWhereClauses(WhereClause[] where)
    {
        List<String> output = new ArrayList<>();
        
        for (int i = 0; i < where.length; i++)
        {
            if (tableColumns.containsKey(where[i].getColumnId())
                    && !tableColumns.get(where[i].getColumnId()).isDisabled())
            {
                output.add(tableColumns.get(where[i].getColumnId()).getName());
                output.add(where[i].getOperator());
                output.add(where[i].getValue());
            }
        }
        
        return output.toArray(new String[output.size()]);
    }
    
    private List<Map<String, String>> copyResultSet(ResultSet rs, Set<String> columns) throws SQLException
    {
        ImmutableList.Builder<Map<String, String>> result = new ImmutableList.Builder<>();
        
        if (rs.isBeforeFirst())
        {
            while (rs.next())
            {
                Map<String, String> row = new HashMap<>();
                
                for (String id : columns)
                {
                    row.put(id, rs.getString(getColumnName(id)));
                }
                
                result.add(row);
            }
            
            rs.close();
        }
        
        return result.build();
    }
    
    private final Database database;
    private final String table;
    private final Map<String, Column> tableColumns = new HashMap<>();
}
