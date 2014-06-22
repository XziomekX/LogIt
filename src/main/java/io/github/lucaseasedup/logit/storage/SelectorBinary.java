/*
 * SelectorBinary.java
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

public final class SelectorBinary extends Selector
{
    public SelectorBinary(Selector leftOperand,
                          SelectorBinary.Relation relation,
                          Selector rightOperand)
    {
        if (leftOperand == null || relation == null || rightOperand == null)
            throw new IllegalArgumentException();
        
        this.leftOperand = leftOperand;
        this.relation = relation;
        this.rightOperand = rightOperand;
    }
    
    public Selector getLeftOperand()
    {
        return leftOperand;
    }
    
    public SelectorBinary.Relation getRelation()
    {
        return relation;
    }
    
    public Selector getRightOperand()
    {
        return rightOperand;
    }
    
    public enum Relation
    {
        AND, OR;
    }
    
    private final Selector leftOperand;
    private final SelectorBinary.Relation relation;
    private final Selector rightOperand;
}
