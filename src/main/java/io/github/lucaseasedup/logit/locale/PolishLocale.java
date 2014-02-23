/*
 * PolishLocale.java
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

@LocalePrefix("pl")
public final class PolishLocale implements Locale
{
    private PolishLocale()
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
            
            if (days == 1)
            {
                sb.append(" dzien");
            }
            else
            {
                sb.append(" dni");
            }
            
            if (hours > 0 || minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (hours > 0)
        {
            sb.append(hours);
            
            if (hours == 1)
            {
                sb.append(" godzina");
            }
            else if (hours >= 2 && hours <= 4)
            {
                sb.append(" godziny");
            }
            else
            {
                sb.append(" godzin");
            }
            
            if (minutes > 0 || seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (minutes > 0)
        {
            sb.append(minutes);
            
            if (minutes == 1)
            {
                sb.append(" minuta");
            }
            else if (minutes >= 2 && minutes <= 4)
            {
                sb.append(" minuty");
            }
            else
            {
                sb.append(" minut");
            }
            
            if (seconds > 0)
            {
                sb.append(", ");
            }
        }
        
        if (seconds > 0)
        {
            sb.append(seconds);
            
            if (minutes == 1)
            {
                sb.append(" sekunda");
            }
            else if (minutes >= 2 && minutes <= 4)
            {
                sb.append(" sekundy");
            }
            else
            {
                sb.append(" sekund");
            }
        }
        
        return sb.toString();
    }
    
    public static PolishLocale getInstance()
    {
        return INSTANCE;
    }
    
    private static final PolishLocale INSTANCE = new PolishLocale();
}
