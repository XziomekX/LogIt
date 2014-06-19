/*
 * HashingAlgorithm.java
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
package io.github.lucaseasedup.logit.security;

public enum HashingAlgorithm
{
    PLAIN("plain"),
    MD2("md2"), MD5("md5"),
    SHA1("sha-1"), SHA256("sha-256"), SHA384("sha-384"), SHA512("sha-512"),
    WHIRLPOOL("whirlpool"), BCRYPT("bcrypt"), AUTHME("authme");
    
    private HashingAlgorithm(String name)
    {
        assert name != null;
        
        this.name = name;
    }
    
    /**
     * Returns a string representation of this {@code HashingAlgorithm}.
     * 
     * @return the string representation of this {@code HashingAlgorithm}.
     */
    public String encode()
    {
        return name;
    }
    
    /**
     * Decodes a string into a {@code HashingAlgorithm}.
     * 
     * @param name string representation of a {@code HashingAlgorithm}.
     * 
     * @return the corresponding {@code HashingAlgorithm},
     *         or {@code null} if no {@code HashingAlgorithm} was found for the given string.
     */
    public static HashingAlgorithm decode(String name)
    {
        for (HashingAlgorithm value : values())
        {
            if (value == AUTHME && name.startsWith("authme:"))
            {
                return value;
            }
            
            if (value.encode().equals(name))
            {
                return value;
            }
        }
        
        return null;
    }
    
    private final String name;
}
