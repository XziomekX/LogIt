/*
 * SelectorCondition.java
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
package io.github.lucaseasedup.logit.storage;

public final class SelectorCondition extends Selector
{
    public SelectorCondition(String key, Relation relation, String value)
    {
        if (key == null || relation == null)
            throw new NullPointerException();
        
        this.key = key;
        this.relation = relation;
        this.value = value;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public Relation getRelation()
    {
        return relation;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public enum Relation
    {
        EQUALS, LESS_THAN, GREATER_THAN, STARTS_WITH, ENDS_WITH, CONTAINS;
    }
    
    private final String key;
    private final Relation relation;
    private final String value;
}
