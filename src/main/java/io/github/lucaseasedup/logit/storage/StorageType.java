/*
 * StorageType.java
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

public enum StorageType
{
    UNKNOWN, NONE, SQLITE, MYSQL, H2, POSTGRESQL, CSV;
    
    public static StorageType decode(String s)
    {
        switch (s.toLowerCase())
        {
        case "none":       return NONE;
        case "sqlite":     return SQLITE;
        case "mysql":      return MYSQL;
        case "h2":         return H2;
        case "postgresql": return POSTGRESQL;
        case "csv":        return CSV;
        default:           return UNKNOWN;
        }
    }
    
    /**
     * Converts this {@code StorageType} to a string representation.
     * 
     * @return the string representation of this {@code StorageType},
     *         or {@code null} if no representation for this
     *         {@code StorageType} was implemented.
     */
    public String encode()
    {
        switch (this)
        {
        case NONE:       return "none";
        case SQLITE:     return "sqlite";
        case MYSQL:      return "mysql";
        case H2:         return "h2";
        case POSTGRESQL: return "postgresql";
        case CSV:        return "csv";
        default:         return null;
        }
    }
}
