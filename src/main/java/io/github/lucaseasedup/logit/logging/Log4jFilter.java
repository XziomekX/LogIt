/*
 * Log4jFilter.java
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
package io.github.lucaseasedup.logit.logging;

import java.util.regex.Matcher;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

public final class Log4jFilter
{
    public Log4jFilter(CommandSilencer commandSilencer)
    {
        if (commandSilencer == null)
            throw new IllegalArgumentException();
        
        this.commandSilencer = commandSilencer;
    }
    
    public void register()
    {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        
        rootLogger.addFilter(new Filter()
        {
            @Override
            public Result filter(LogEvent event)
            {
                if (commandSilencer.isDisposed())
                    return Result.NEUTRAL;
                
                if (!event.getLoggerName().endsWith(".PlayerConnection"))
                    return Result.NEUTRAL;
                
                Matcher matcher = commandSilencer.getMatcherForMsg(
                        event.getMessage().getFormattedMessage()
                );
                
                if (matcher.find())
                {
                    String label = matcher.group(1);
                    
                    if (commandSilencer.isCommandSilenced(label))
                    {
                        return Result.DENY;
                    }
                }
                
                return Result.NEUTRAL;
            }
            
            @Override
            public Result filter(Logger logger,
                                 Level level,
                                 Marker marker,
                                 Message msg,
                                 Throwable t)
            {
                return Result.NEUTRAL;
            }
            
            @Override
            public Result filter(Logger logger,
                                 Level level,
                                 Marker marker,
                                 Object msg,
                                 Throwable t)
            {
                return Result.NEUTRAL;
            }
            
            @Override
            public Result filter(Logger logger,
                                 Level level,
                                 Marker marker,
                                 String msg,
                                 Object... params)
            {
                return Result.NEUTRAL;
            }
            
            @Override
            public Result getOnMismatch()
            {
                return Result.NEUTRAL;
            }
            
            @Override
            public Result getOnMatch()
            {
                return Result.NEUTRAL;
            }
        });
    }
    
    private final CommandSilencer commandSilencer;
}
