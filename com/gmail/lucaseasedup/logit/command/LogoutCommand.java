package com.gmail.lucaseasedup.logit.command;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import static java.util.logging.Level.INFO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand implements CommandExecutor
{
    public LogoutCommand(LogItCore core)
    {
        this.core = core;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        if (!cmd.getName().equalsIgnoreCase("logout"))
            return false;
        
        Player p = null;
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        if ((args.length > 0 && !args[0].equals("-x")) || args.length > 2)
        {
            s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION", p != null));
            return true;
        }
        
        if (args.length > 0 && args[0].equals("-x"))
        {
            if (p != null && !p.hasPermission("logit.logout.others"))
            {
                s.sendMessage(getMessage("NO_PERMS", true));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING", p != null).replace("%param%", "player"));
                return true;
            }
            if (!isPlayerOnline(args[1]))
            {
                s.sendMessage(getMessage("NOT_ONLINE", p != null).replace("%player%", args[1]));
                return true;
            }
            if (!core.getSessionManager().isSessionAlive(args[1]))
            {
                s.sendMessage(getMessage("NOT_LOGGED_IN_OTHERS", p != null).replace("%player%", args[1]));
                return true;
            }
            
            core.putIntoWaitingRoom(getPlayer(args[1]));
            core.getSessionManager().endSession(getPlayer(args[1]), true);
            
            if (p != null && !core.getConfig().getForceLogin())
            {
                p.sendMessage(getMessage("LOGGED_OUT_OTHERS", true).replace("%player%", args[1]));
            }
        }
        else
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (!p.hasPermission("logit.logout.self"))
            {
                s.sendMessage(getMessage("NO_PERMS", true));
                return true;
            }
            if (!core.getSessionManager().isSessionAlive(p))
            {
                s.sendMessage(getMessage("NOT_LOGGED_IN_SELF", true));
                return true;
            }
            
            if (core.getConfig().getForceLogin())
            {
                core.putIntoWaitingRoom(p);
            }
            
            core.getSessionManager().endSession(p, true);
        }
        
        return true;
    }
    
    private LogItCore core;
}
