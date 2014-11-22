package io.github.lucaseasedup.logit.logging;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.regex.Matcher;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.bukkit.entity.Player;

public final class Log4jFilter extends LogItCoreObject
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
                if (commandSilencer.isFiltersRegistered())
                    return Result.NEUTRAL;
                
                if (event.getMessage().getFormattedMessage() == null)
                    return Result.NEUTRAL;
                
                if (!event.getLoggerName().endsWith(".PlayerConnection"))
                    return Result.NEUTRAL;
                
                Matcher matcher = commandSilencer.getMatcherForMsg(
                        event.getMessage().getFormattedMessage()
                );
                
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
                        return Result.DENY;
                    }
                    
                    if (commandSilencer.isCommandSilenced(label))
                    {
                        return Result.DENY;
                    }
                }
                
                return Result.NEUTRAL;
            }
            
            @Override
            public Result filter(
                    Logger logger,
                    Level level,
                    Marker marker,
                    Message msg,
                    Throwable t
            )
            {
                return Result.NEUTRAL;
            }
            
            @Override
            public Result filter(
                    Logger logger,
                    Level level,
                    Marker marker,
                    Object msg,
                    Throwable t
            )
            {
                return Result.NEUTRAL;
            }
            
            @Override
            public Result filter(
                    Logger logger,
                    Level level,
                    Marker marker,
                    String msg,
                    Object... params
            )
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
