package com.gmail.lucaseasedup.logit.command;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import static java.util.logging.Level.INFO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor
{
    public RegisterCommand(LogItCore core)
    {
        this.core = core;
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        if (!cmd.getName().equalsIgnoreCase("register"))
            return false;
        
        Player p = null;
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        if ((args.length > 1 && !args[0].equals("-x")) || args.length > 3)
        {
            s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION", p != null));
            return true;
        }
        
        if (args.length > 0 && args[0].equals("-x"))
        {
            if (p != null && ((core.isPlayerForcedToLogin(p) && !core.getSessionManager().isSessionAlive(p)) || !p.hasPermission("logit.register.others")))
            {
                s.sendMessage(getMessage("NO_PERMS", true));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING", true).replace("%param%", "player"));
                return true;
            }
            if (args.length < 3)
            {
                s.sendMessage(getMessage("PARAM_MISSING", true).replace("%param%", "password"));
                return true;
            }
            if (core.isRegistered(args[1]))
            {
                s.sendMessage(getMessage("ALREADY_REGISTERED_OTHERS", p != null).replace("%player%", args[1]));
                return true;
            }
            if (args[2].length() < core.getConfig().getPasswordMinLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_SHORT", p != null).replace("%min-length%", String.valueOf(core.getConfig().getPasswordMinLength())));
                return true;
            }
            
            core.register(args[1], args[2], true);
            
            if (p != null && !core.getConfig().getForceLogin())
            {
                p.sendMessage(getMessage("REGISTERED_OTHERS", true).replace("%player%", args[1]));
            }
        }
        else
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (!p.hasPermission("logit.register.self"))
            {
                p.sendMessage(getMessage("NO_PERMS", true));
                return true;
            }
            if (args.length < 1)
            {
                s.sendMessage(getMessage("PARAM_MISSING", p != null).replace("%param%", "password"));
                return true;
            }
            if (core.isRegistered(p))
            {
                p.sendMessage(getMessage("ALREADY_REGISTERED_SELF", true));
                return true;
            }
            if (args[0].length() < core.getConfig().getPasswordMinLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_SHORT", true).replace("%min-length%", String.valueOf(core.getConfig().getPasswordMinLength())));
                return true;
            }
            
            core.register(p.getName(), args[0], !core.getConfig().getForceLogin());
            core.getSessionManager().startSession(p, true);
        }
        
        return true;
    }
    
    private LogItCore core;
}
