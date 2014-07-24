/*
 * TimeString.java
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

import io.github.lucaseasedup.logit.config.validators.TimeStringValidator;
import java.util.regex.Matcher;

public final class TimeString
{
    private TimeString()
    {
    }
    
    public static long decode(String string, TimeUnit convertTo)
    {
        if (string == null || convertTo == null)
            throw new IllegalArgumentException();
        
        Matcher matcher = TimeStringValidator.PATTERN.matcher(string);
        long time = 0;
        
        while (matcher.find())
        {
            String longValue = matcher.group(1);
            TimeUnit unit = TimeUnit.decode(matcher.group(2));
            
            if (unit != null)
            {
                time += unit.convert(Long.parseLong(longValue), convertTo);
            }
        }
        
        return time;
    }
}
