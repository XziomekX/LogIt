/*
 * LoginCommand.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.command;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor
{
    public LoginCommand(LogItCore core)
    {
        this.core = core;
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        if (!cmd.getName().equalsIgnoreCase("login"))
            return false;
        
        Player p = null;
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        if ((args.length > 1 && !args[0].equals("-x")) || args.length > 2)
        {
            s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION", p != null));
            return true;
        }
        
        if (args.length > 0 && args[0].equals("-x"))
        {
            if (p != null && ((core.isPlayerForcedToLogin(p) && !core.getSessionManager().isSessionAlive(p)) || !p.hasPermission("logit.login.others")))
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
            if (!core.isRegistered(args[1]))
            {
                s.sendMessage(getMessage("NOT_REGISTERED_OTHERS", p != null).replace("%player%", args[1]));
                return true;
            }
            if (core.getSessionManager().isSessionAlive(args[1]))
            {
                s.sendMessage(getMessage("ALREADY_LOGGED_IN_OTHERS", p != null).replace("%player%", args[1]));
                return true;
            }
            
            core.getSessionManager().startSession(getPlayer(args[1]), true);
            
            if (p != null && !core.getConfig().getForceLogin())
            {
                p.sendMessage(getMessage("LOGGED_IN_OTHERS", true).replace("%player%", args[1]));
            }
        }
        else
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (!p.hasPermission("logit.login.self"))
            {
                p.sendMessage(getMessage("NO_PERMS", true));
                return true;
            }
            if (args.length < 1)
            {
                s.sendMessage(getMessage("PARAM_MISSING", true).replace("%param%", "password"));
                return true;
            }
            if (!core.isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("NOT_REGISTERED_SELF", true));
                return true;
            }
            if (core.getSessionManager().isSessionAlive(p.getName()))
            {
                p.sendMessage(getMessage("ALREADY_LOGGED_IN_SELF", true));
                return true;
            }
            if (!core.checkPassword(p.getName(), args[0]) && !core.checkGlobalPassword(args[0]))
            {
                p.sendMessage(getMessage("INCORRECT_PASSWORD", true));
                
                if (!loginRetries.containsKey(p.getName().toLowerCase()))
                {
                    loginRetries.put(p.getName().toLowerCase(), 0);
                }
                
                loginRetries.put(p.getName().toLowerCase(), loginRetries.get(p.getName().toLowerCase()) + 1);
                
                if (loginRetries.get(p.getName().toLowerCase()) >= core.getConfig().getLoginFailsToKick())
                {
                    loginRetries.remove(p.getName().toLowerCase());
                    p.kickPlayer(getMessage("TOO_MANY_LOGIN_FAILS", true));
                }
                
                return true;
            }
            
            core.getSessionManager().startSession(p, true);
        }
        
        return true;
    }
    
    private LogItCore core;
    
    private HashMap<String, Integer> loginRetries = new HashMap<>();
}
