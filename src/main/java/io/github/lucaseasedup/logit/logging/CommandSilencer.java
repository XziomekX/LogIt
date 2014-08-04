/*
 * CommandSilencer.java
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

import io.github.lucaseasedup.logit.common.Disposable;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

public final class CommandSilencer implements Disposable
{
    public CommandSilencer(Collection<PluginCommand> silencedCommands)
    {
        if (silencedCommands == null)
            throw new IllegalArgumentException();
        
        for (PluginCommand command : silencedCommands)
        {
            if (command == null)
            {
                throw new IllegalArgumentException();
            }
        }
        
        this.silencedCommands = new HashSet<>(silencedCommands);
    }
    
    @Override
    public void dispose()
    {
        if (silencedCommands != null)
        {
            silencedCommands.clear();
            silencedCommands = null;
        }
        
        Bukkit.getLogger().setFilter(null);
    }
    
    public void registerFilters()
    {
        Bukkit.getLogger().setFilter(new Filter()
        {
            @Override
            public boolean isLoggable(LogRecord record)
            {
                if (isDisposed())
                    return true;
                
                if (!record.getLoggerName().equals("Minecraft-Server"))
                    return true;
                
                // Short-circut with fast contains() call.
                if (!record.getMessage().contains(" issued server command: /"))
                    return true;
                
                /* Use regular expression to check if this command should be silenced. */
                Matcher matcher = getMatcherForMsg(record.getMessage());
                
                if (matcher.find())
                {
                    String label = matcher.group(1);
                    
                    if (isCommandSilenced(label))
                    {
                        return false;
                    }
                }
                
                return true;
            }
        });
        
        try
        {
            Class.forName("org.apache.logging.log4j.core.Filter");
            
            new Log4jFilter(this).register();
        }
        catch (ClassNotFoundException | NoClassDefFoundError ex)
        {
            // If the class was not found, it means we're running an old
            // version of CraftBukkit. Standard logger will be used instead.
        }
    }
    
    /* package */ boolean isCommandSilenced(String label)
    {
        if (label == null)
            throw new IllegalArgumentException();
        
        for (PluginCommand command : silencedCommands)
        {
            if (command.getLabel().equalsIgnoreCase(label))
                return true;
            
            if (hasAlias(command, label))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /* package */ boolean hasAlias(PluginCommand command, String label)
    {
        if (command == null || label == null)
            throw new IllegalArgumentException();
        
        return CollectionUtils.containsIgnoreCase(label, command.getAliases());
    }
    
    /* package */ boolean isDisposed()
    {
        return silencedCommands == null;
    }
    
    /* package */ Matcher getMatcherForMsg(String msg)
    {
        if (msg == null)
            throw new IllegalArgumentException();
        
        return SERVER_COMMAND_PATTERN.matcher(msg);
    }
    
    private static final Pattern SERVER_COMMAND_PATTERN = Pattern
            .compile(" issued server command: /([A-Za-z]+)");
    
    private Collection<PluginCommand> silencedCommands;
}
