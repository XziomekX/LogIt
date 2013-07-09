/*
 * SqlUtils.java
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
package io.github.lucaseasedup.logit.util;

/**
 * @author LucasEasedUp
 */
public class SqlUtils
{
    private SqlUtils()
    {
    }
    
    public static String escapeQuotes(String string, String quote)
    {
        return string.replace(quote, quote + quote).replace("\\", "\\\\");
    }
    
    public static String implodeColumnArray(String[] columns)
    {
        StringBuilder output = new StringBuilder();
        
        if (columns.length >= 1)
        {
            if (columns[0].equals("*"))
                output.append("*");
            else
                output.append("`").append(escapeQuotes(columns[0], "`")).append("`");
        }
        
        for (int i = 1; i < columns.length; i++)
        {
            output.append(", `").append(escapeQuotes(columns[i], "`")).append("`");
        }
        
        return output.toString();
    }
    
    public static String implodeColumnDefinition(String[] columns)
    {
        StringBuilder output = new StringBuilder();
        
        if (columns.length >= 2)
        {
            output.append("`").append(escapeQuotes(columns[0], "`")).append("` ").append(columns[1]);
        }
        
        for (int i = 2; i < columns.length; i += 2)
        {
            output.append(", `").append(escapeQuotes(columns[i], "`")).append("` ").append(columns[i + 1]);
        }
        
        return output.toString();
    }
    
    public static String implodeValueArray(String[] values)
    {
        StringBuilder output = new StringBuilder();
        
        if (values.length >= 1)
        {
            output.append("'").append(escapeQuotes(values[0], "'")).append("'");
        }
        
        for (int i = 1; i < values.length; i++)
        {
            if (values[i] != null)
                output.append(", '").append(escapeQuotes(values[i], "'")).append("'");
            else
                output.append(", NULL");
        }
        
        return output.toString();
    }
    
    public static String implodeSetArray(String[] set)
    {
        StringBuilder output = new StringBuilder();
        
        if (set.length >= 2)
        {
            output.append("`").append(escapeQuotes(set[0], "`")).append("` = '").append(escapeQuotes(set[1], "'")).append("'");
        }
        
        for (int i = 2; i < set.length; i += 2)
        {
            output.append(", `").append(escapeQuotes(set[i], "`")).append("` = '").append(escapeQuotes(set[i + 1], "'")).append("'");
        }
        
        return output.toString();
    }
    
    public static String implodeWhereArray(String[] conditions)
    {
        StringBuilder output = new StringBuilder();
        
        if (conditions.length >= 3)
        {
            output.append("`").append(escapeQuotes(conditions[0], "`")).append("` ").append(conditions[1]).append(" '")
                .append(escapeQuotes(conditions[2], "'")).append("'");
        }
        
        for (int i = 3; i < conditions.length; i += 3)
        {
            output.append(" AND `").append(escapeQuotes(conditions[i], "`")).append("` ").append(conditions[i + 1]).append(" '")
                .append(escapeQuotes(conditions[i + 2], "'")).append("'");
        }
        
        return output.toString();
    }
}
