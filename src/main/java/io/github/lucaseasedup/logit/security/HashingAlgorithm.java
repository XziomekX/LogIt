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
    UNKNOWN, PLAIN, MD2, MD5, SHA1, SHA256, SHA384, SHA512, WHIRLPOOL, BCRYPT;
    
    public static HashingAlgorithm decode(String s)
    {
        switch (s.toLowerCase())
        {
        case "plain":     return PLAIN;
        case "md2":       return MD2;
        case "md5":       return MD5;
        case "sha-1":     return SHA1;
        case "sha-256":   return SHA256;
        case "sha-384":   return SHA384;
        case "sha-512":   return SHA512;
        case "whirlpool": return WHIRLPOOL;
        case "bcrypt":    return BCRYPT;
        default:          return UNKNOWN;
        }
    }
    
    /**
     * Converts this {@code HashingAlgorithm} to a string representation.
     * 
     * @return the string representation of this {@code HashingAlgorithm},
     *         or {@code null} if no representation for this
     *         {@code HashingAlgorithm} was implemented.
     */
    public String encode()
    {
        switch (this)
        {
        case PLAIN:     return "plain";
        case MD2:       return "md2";
        case MD5:       return "md5";
        case SHA1:      return "sha-1";
        case SHA256:    return "sha-256";
        case SHA384:    return "sha-384";
        case SHA512:    return "sha-512";
        case WHIRLPOOL: return "whirlpool";
        case BCRYPT:    return "bcrypt";
        default:        return null;
        }
    }
}
