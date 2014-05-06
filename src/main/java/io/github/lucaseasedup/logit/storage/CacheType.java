/*
 * CacheType.java
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

public enum CacheType
{
    DISABLED("disabled"), PRELOADED("preloaded");
    
    private CacheType(String name)
    {
        assert name != null;
        
        this.name = name;
    }
    
    /**
     * Returns a string representation of this {@code CacheType}.
     * 
     * @return the string representation of this {@code CacheType}.
     */
    public String encode()
    {
        return name;
    }
    
    /**
     * Decodes a string into a {@code CacheType}.
     * 
     * @param name string representation of a {@code CacheType}.
     * 
     * @return the corresponding {@code CacheType},
     *         or {@code null} if no {@code CacheType} was found for the given string.
     */
    public static CacheType decode(String name)
    {
        for (CacheType value : values())
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
