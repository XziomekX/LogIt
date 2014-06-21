/*
 * GermanLocale.java
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
package io.github.lucaseasedup.logit.locale;

@LocalePrefix("de")
public final class GermanLocale implements Locale
{
    private GermanLocale()
    {
    }
    
    @Override
    public String stringifySeconds(long seconds)
    {
        StringBuilder sb = new StringBuilder();
        long days, hours, minutes;
        
        days = seconds / 86400L;
        seconds %= 86400;
        hours = seconds / 3600L;
        seconds %= 3600;
        minutes = seconds / 60L;
        seconds %= 60;
        
        if (days > 0)
        {
            sb.append(days);
            sb.append(" Tag");
            
            if (days > 1)
            {
                sb.append("e");
            }
            
            if (hours > 0 || minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (hours > 0)
        {
            sb.append(hours);
            sb.append(" Stunde");
            
            if (hours > 1)
            {
                sb.append("n");
            }
            
            if (minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (minutes > 0)
        {
            sb.append(minutes);
            sb.append(" Minute");
            
            if (minutes > 1)
            {
                sb.append("n");
            }
            
            if (seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (seconds > 0)
        {
            sb.append(seconds);
            sb.append(" Sekunde");
            
            if (seconds > 1)
            {
                sb.append("n");
            }
        }
        
        return sb.toString();
    }
    
    public static GermanLocale getInstance()
    {
        return INSTANCE;
    }
    
    private static final GermanLocale INSTANCE = new GermanLocale();
}
