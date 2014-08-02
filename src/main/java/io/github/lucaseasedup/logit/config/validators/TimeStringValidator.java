/*
 * TimeStringValidator.java
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
package io.github.lucaseasedup.logit.config.validators;

import io.github.lucaseasedup.logit.config.PropertyType;
import io.github.lucaseasedup.logit.config.PropertyValidator;
import io.github.lucaseasedup.logit.config.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeStringValidator implements PropertyValidator
{
    @Override
    public boolean validate(String path, PropertyType type, Object value)
    {
        if (value == null || !(value instanceof String))
            return false;
        
        Matcher matcher = PATTERN.matcher((String) value);
        
        if (!matcher.find())
            return false;
        
        do
        {
            String longValue = matcher.group(1);
            TimeUnit unit = TimeUnit.decode(matcher.group(2));
            
            if (unit == null)
                return false;
            
            try
            {
                Long.parseLong(longValue);
            }
            catch (NumberFormatException ex)
            {
                return false;
            }
        }
        while (matcher.find());
        
        return true;
    }
    
    public static final Pattern PATTERN =
            Pattern.compile("(?<=^|[A-Za-z]\\s)\\s*([0-9]+)\\s*([A-Za-z]+)\\s*(?=\\s|$)",
                    Pattern.MULTILINE);
}
