/*
 * ArrayUtils.java
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
public class ArrayUtils
{
    private ArrayUtils()
    {
    }
    
    public static String implodeKeyValueArray(String[] pieces, String glue, String assignmentSign, String beforeValue, String afterValue)
    {
        String output = "";
        
        if (pieces.length > 1)
        {
            output += pieces[0] + assignmentSign + beforeValue + pieces[1] + afterValue;
            
            for (int i = 2; i < pieces.length; i += 2)
            {
                output += glue + pieces[i] + assignmentSign + beforeValue + pieces[i + 1] + afterValue;
            }
        }
        
        return output;
    }
    
    public static String implodeArray(String[] pieces, String glue, String before, String after)
    {
        String output = "";
        
        if (pieces.length > 0)
        {
            output += before + pieces[0] + after;
            
            for (int i = 1; i < pieces.length; i++)
            {
                output += glue + before + pieces[i] + after;
            }
        }
        
        return output;
    }
    
    public static String implodeArray(String[] pieces, String glue)
    {
        return implodeArray(pieces, glue, "", "");
    }
    
    public static String implodeArray(String[] pieces)
    {
        return implodeArray(pieces, "");
    }
}
