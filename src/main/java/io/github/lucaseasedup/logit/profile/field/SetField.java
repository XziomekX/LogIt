/*
 * SetField.java
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
package io.github.lucaseasedup.logit.profile.field;

import java.util.Collection;

public final class SetField extends Field
{
    public SetField(String name, Collection<String> values)
    {
        super(name);
        
        if (values == null)
            throw new NullPointerException();
        
        this.values = values;
    }
    
    public boolean isAccepted(String value)
    {
        for (String s : values)
        {
            if ((s == null && value == null) || (s != null && s.equals(value)))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public Collection<String> getAcceptedValues()
    {
        return values;
    }
    
    private final Collection<String> values;
}
