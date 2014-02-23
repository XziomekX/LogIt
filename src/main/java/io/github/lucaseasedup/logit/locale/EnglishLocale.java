/*
 * EnglishLocale.java
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

@LocalePrefix("en")
public final class EnglishLocale implements Locale
{
    private EnglishLocale()
    {
    }
    
    @Override
    public String stringifySeconds(int seconds)
    {
        StringBuilder sb = new StringBuilder();
        int days, hours, minutes;
        
        days = seconds / 86400;
        seconds %= 86400;
        hours = seconds / 3600;
        seconds %= 3600;
        minutes = seconds / 60;
        seconds %= 60;
        
        if (days > 0)
        {
            sb.append(days);
            sb.append(" day");
            
            if (days > 1)
            {
                sb.append("s");
            }
            
            if (hours > 0 || minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (hours > 0)
        {
            sb.append(hours);
            sb.append(" hour");
            
            if (hours > 1)
            {
                sb.append("s");
            }
            
            if (minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (minutes > 0)
        {
            sb.append(minutes);
            sb.append(" minute");
            
            if (minutes > 1)
            {
                sb.append("s");
            }
            
            if (seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (seconds > 0)
        {
            sb.append(seconds);
            sb.append(" second");
            
            if (seconds > 1)
            {
                sb.append("s");
            }
        }
        
        return sb.toString();
    }
    
    public static EnglishLocale getInstance()
    {
        return INSTANCE;
    }
    
    private static final EnglishLocale INSTANCE = new EnglishLocale();
}
