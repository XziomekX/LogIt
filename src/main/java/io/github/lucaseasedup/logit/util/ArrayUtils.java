/*
 * ArrayUtils.java
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
package io.github.lucaseasedup.logit.util;

import java.lang.reflect.Array;

public final class ArrayUtils
{
    private ArrayUtils()
    {
    }
    
    public static int[][] copy(int[][] src)
    {
        int[][] dest = new int[src.length][src[0].length];
        
        for (int i = 0; i < src.length; i++)
        {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
        
        return dest;
    }
    
    public static <T> T[][] copy(Class<T> clazz, T[][] src)
    {
        @SuppressWarnings("unchecked")
        T[][] dest = (T[][]) Array.newInstance(clazz, src.length, src[0].length);
        
        for (int i = 0; i < src.length; i++)
        {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
        
        return dest;
    }
}
