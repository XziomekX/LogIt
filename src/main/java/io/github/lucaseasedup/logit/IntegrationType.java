/*
 * IntegrationType.java
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
package io.github.lucaseasedup.logit;

public enum IntegrationType
{
    NONE("none"), PHPBB2("phpbb2");
    
    private IntegrationType(String name)
    {
        assert name != null;
        
        this.name = name;
    }
    
    /**
     * Returns a string representation of this {@code IntegrationType}.
     * 
     * @return the string representation of this {@code IntegrationType}.
     */
    public String encode()
    {
        return name;
    }
    
    /**
     * Decodes a string into an {@code IntegrationType}.
     * 
     * @param name string representation of an {@code IntegrationType}.
     * 
     * @return the corresponding {@code IntegrationType},
     *         or {@code null} if no {@code IntegrationType} was found for the given string.
     * 
     * @throws IllegalArgumentException if {@code name} is {@code null}.
     */
    public static IntegrationType decode(String name)
    {
        if (name == null)
            throw new IllegalArgumentException();
        
        for (IntegrationType value : values())
        {
            if (value.encode().equals(name))
            {
                return value;
            }
        }
        
        return null;
    }
    
    private final String name;
}
