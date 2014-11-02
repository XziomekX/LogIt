package io.github.lucaseasedup.logit.logging;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public final class CommandSilencer extends LogItCoreObject
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
                    String username = matcher.group(1);
                    String label = matcher.group(2);
                    Player player = PlayerUtils.getPlayer(username);
                    
                    // If the player isn't logged in and has to log in, do not
                    // show any commands issued -- they might have mistakenly
                    // typed e.g. "/logni 1234" instead of "/login 1234".
                    if (!getSessionManager().isSessionAlive(player)
                            && getCore().isPlayerForcedToLogIn(player))
                    {
                        return false;
                    }
                    
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
            .compile("(.+) issued server command: /([A-Za-z]+)");
    
    private Collection<PluginCommand> silencedCommands;
}
