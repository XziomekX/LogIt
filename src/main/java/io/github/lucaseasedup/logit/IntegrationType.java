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
    UNKNOWN, NONE, PHPBB2;
    
    public static IntegrationType decode(String s)
    {
        switch (s.toLowerCase())
        {
        case "none":   return NONE;
        case "phpbb2": return PHPBB2;
        default:       return UNKNOWN;
        }
    }
    
    /**
     * Converts this {@code IntegrationType} to a string representation.
     * 
     * @return the string representation of this {@code IntegrationType},
     *         or {@code null} if no representation for this
     *         {@code IntegrationType} was implemented.
     */
    public String encode()
    {
        switch (this)
        {
        case NONE:   return "plain";
        case PHPBB2: return "phpbb2";
        default:     return null;
        }
    }
}
