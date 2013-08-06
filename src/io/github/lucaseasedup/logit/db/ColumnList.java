/*
 * ColumnList.java
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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author LucasEasedUp
 */
public class ColumnList extends ArrayList<String>
{
    public ColumnList()
    {
    }
    
    public ColumnList(Collection<? extends String> c)
    {
        for (String el : c)
        {
            this.add(el);
        }
    }
    
    @Override
    public final boolean add(String column)
    {
        if (!super.contains(column))
        {
            return super.add(column);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean contains(Object o)
    {
        for (Object el : this)
        {
            if ((el instanceof String) && (o instanceof String)
                && ((String) el).equalsIgnoreCase((String) o))
                return true;
            else if ((el instanceof Character) && (o instanceof Character)
                && ((Character) el).toString().equalsIgnoreCase(((Character) o).toString()))
                return true;
            else if (el.equals(o))
                return true;
        }
        
        return false;
    }
    
    @Override
    public int indexOf(Object o)
    {
        for (int i = 0; i < size(); i++)
        {
            Object el = get(i);
            
            if ((el instanceof String) && (o instanceof String)
                && ((String) el).equalsIgnoreCase((String) o))
                return i;
            else if ((el instanceof Character) && (o instanceof Character)
                && ((Character) el).toString().equalsIgnoreCase(((Character) o).toString()))
                return i;
            else if (el.equals(o))
                return i;
        }
        
        return -1;
    }
}
