/*
 * WhereClause.java
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

/**
 * @author LucasEasedUp
 */
public final class WhereClause
{
    public WhereClause(String columnId, String operator, String value)
    {
        this.columnId = columnId;
        this.operator = operator;
        this.value = value;
    }
    
    public String getColumnId()
    {
        return columnId;
    }
    
    public String getOperator()
    {
        return operator;
    }
    
    public String getValue()
    {
        return value;
    }

    public static final String EQUAL = "=";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN = ">";
    
    private final String columnId;
    private final String operator;
    private final String value;
}
