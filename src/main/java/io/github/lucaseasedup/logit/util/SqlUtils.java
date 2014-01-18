/*
 * SqlUtils.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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

public final class SqlUtils
{
    private SqlUtils()
    {
    }
    
    public static String escapeQuotes(String string, String quote, boolean escapeBackslashes)
    {
        string = string.replace(quote, quote + quote);
        
        if (escapeBackslashes)
        {
            string = string.replace("\\", "\\\\");
        }
        
        return string;
    }

    public static String buildColumnDefinition(String[] columns, String quote, boolean escapeBackslashes)
    {
        if ((columns.length % 2) != 0)
            throw new IllegalArgumentException("Length of columns must be multiple of 2.");
        
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < columns.length; i += 2)
        {
            if (output.length() != 0)
            {
                output.append(", ");
            }
            
            // Column name
            output.append(quote);
            output.append(escapeQuotes(columns[i], quote, escapeBackslashes));
            output.append(quote);
            
            // Column type
            output.append(" ");
            output.append(columns[i + 1]);
        }
        
        return output.toString();
    }
    
    public static String implodeColumns(String[] columns, String quote, boolean escapeBackslashes)
    {
        StringBuilder output = new StringBuilder();
        
        if (columns.length == 1 && columns[0].equals("*"))
            return "*";
        
        for (int i = 0; i < columns.length; i++)
        {
            if (output.length() != 0)
            {
                output.append(", ");
            }
            
            output.append(quote);
            output.append(escapeQuotes(columns[i], quote, escapeBackslashes));
            output.append(quote);
        }
        
        return output.toString();
    }
    
    public static String implodeValues(String[] values, String valueQuote, boolean escapeBackslashes)
    {
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < values.length; i++)
        {
            if (output.length() != 0)
            {
                output.append(", ");
            }
            
            if (values[i] == null)
            {
                output.append("NULL");
            }
            else
            {
                output.append(valueQuote);
                output.append(escapeQuotes(values[i], valueQuote, escapeBackslashes));
                output.append(valueQuote);
            }
        }
        
        return output.toString();
    }
    
    public static String implodeSet(String[] set,
                                         String columnQuote,
                                         String valueQuote,
                                         boolean escapeBackslashes)
    {
        if ((set.length % 2) != 0)
            throw new IllegalArgumentException("Length of set must be multiple of 2.");
        
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < set.length; i += 2)
        {
            if (output.length() != 0)
            {
                output.append(", ");
            }
            
            // Column name
            output.append(columnQuote);
            output.append(escapeQuotes(set[i], columnQuote, escapeBackslashes));
            output.append(columnQuote);
            
            // Assignment operator
            output.append(" = ");
            
            // Value
            output.append(valueQuote);
            output.append(escapeQuotes(set[i + 1], valueQuote, escapeBackslashes));
            output.append(valueQuote);
        }
        
        return output.toString();
    }
    
    public static String implodeWhere(String[] conditions,
                                           String columnQuote,
                                           String valueQuote,
                                           boolean escapeBackslashes)
    {
        if ((conditions.length % 3) != 0)
            throw new IllegalArgumentException("Length of conditions must be multiple of 3.");
        
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < conditions.length; i += 3)
        {
            if (output.length() != 0)
            {
                output.append(" AND ");
            }
            
            // Column name
            output.append(columnQuote);
            output.append(escapeQuotes(conditions[i], columnQuote, escapeBackslashes));
            output.append(columnQuote);
            
            // Operator
            output.append(" ");
            output.append(conditions[i + 1]);
            output.append(" ");
            
            // Value
            output.append(valueQuote);
            output.append(escapeQuotes(conditions[i + 2], valueQuote, escapeBackslashes));
            output.append(valueQuote);
        }
        
        return output.toString();
    }
}
