/*
 * IoUtils.java
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
package io.github.lucaseasedup.logit.util;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStream;

/**
 * @author LucasEasedUp
 */
public final class IoUtils
{
    private IoUtils()
    {
    }
    
    public static String toString(InputStream is) throws IOException
    {
        StringWriter sw = new StringWriter();
        InputStreamReader isr = new InputStreamReader(is);
        
        char[] buffer = new char[1024 * 4];
        int n = 0;
        
        while (-1 != (n = isr.read(buffer)))
        {
            sw.write(buffer, 0, n);
        }
        
        return sw.toString();
    }
}
