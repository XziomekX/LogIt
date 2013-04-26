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
package com.gmail.lucaseasedup.logit.util;

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
        return string.replace(quote, quote + quote);
    }
    
    public static String implodeColumnArray(String[] columns)
    {
        String output = "";
        
        if (columns.length >= 1)
        {
            if (columns[0].equals("*"))
                output += "*";
            else
                output += "`" + escapeQuotes(columns[0], "`") + "`";
        }
        
        for (int i = 1; i < columns.length; i++)
        {
            output += ", `" + escapeQuotes(columns[i], "`") + "`";
        }
        
        return output;
    }
    
    public static String implodeColumnDefinition(String[] columns)
    {
        String output = "";
        
        if (columns.length >= 2)
        {
            output += "`" + escapeQuotes(columns[0], "`") + "` " + columns[1];
        }
        
        for (int i = 2; i < columns.length; i += 2)
        {
            output += ", `" + escapeQuotes(columns[i], "`") + "` " + columns[i + 1];
        }
        
        return output;
    }
    
    public static String implodeValueArray(String[] values)
    {
        String output = "";
        
        if (values.length >= 1)
        {
            output += "'" + escapeQuotes(values[0], "'") + "'";
        }
        
        for (int i = 1; i < values.length; i++)
        {
            if (values[i] != null)
                output += ", '" + escapeQuotes(values[i], "'") + "'";
            else
                output += ", NULL";
        }
        
        return output;
    }
    
    public static String implodeSetArray(String[] set)
    {
        String output = "";
        
        if (set.length >= 2)
        {
            output += "`" + escapeQuotes(set[0], "`") + "` = '" + escapeQuotes(set[1], "'") + "'";
        }
        
        for (int i = 2; i < set.length; i += 2)
        {
            output += ", `" + escapeQuotes(set[i], "`") + "` = '" + escapeQuotes(set[i + 1], "'") + "'";
        }
        
        return output;
    }
    
    public static String implodeWhereArray(String[] conditions)
    {
        String output = "";
        
        if (conditions.length >= 2)
        {
            output += "`" + escapeQuotes(conditions[0], "`") + "` = '" + escapeQuotes(conditions[1], "'") + "'";
        }
        
        for (int i = 2; i < conditions.length; i += 2)
        {
            output += " AND `" + escapeQuotes(conditions[i], "`") + "` = '" + escapeQuotes(conditions[i + 1], "'") + "'";
        }
        
        return output;
    }
}
