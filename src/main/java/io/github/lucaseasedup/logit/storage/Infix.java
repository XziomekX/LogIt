/*
 * Infix.java
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

public final class Infix
{
    private Infix()
    {
    }
    
    public static final SelectorBinary.Relation AND =
            SelectorBinary.Relation.AND;
    public static final SelectorBinary.Relation OR =
            SelectorBinary.Relation.OR;
    
    public static final SelectorCondition.Relation EQUALS =
            SelectorCondition.Relation.EQUALS;
    public static final SelectorCondition.Relation LESS_THAN =
            SelectorCondition.Relation.LESS_THAN;
    public static final SelectorCondition.Relation GREATER_THAN =
            SelectorCondition.Relation.GREATER_THAN;
    public static final SelectorCondition.Relation STARTS_WITH =
            SelectorCondition.Relation.STARTS_WITH;
    public static final SelectorCondition.Relation ENDS_WITH =
            SelectorCondition.Relation.ENDS_WITH;
    public static final SelectorCondition.Relation CONTAINS =
            SelectorCondition.Relation.CONTAINS;
}
