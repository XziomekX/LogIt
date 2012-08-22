/*
 * LogoutCommand.java
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
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 2)
        {
            if (p != null && !p.hasPermission("logit.logout.others"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
                return true;
            }
            if (!isPlayerOnline(args[1]))
            {
                s.sendMessage(getMessage("NOT_ONLINE").replace("%player%", args[1]));
                return true;
            }
            if (!core.getSessionManager().isSessionAlive(args[1]))
            {
                s.sendMessage(getMessage("NOT_LOGGED_IN_OTHERS").replace("%player%", args[1]));
                return true;
            }
            
            core.getSessionManager().endSession(getPlayer(args[1]), true);
            
            if (!core.getConfig().getForceLogin())
            {
                p.sendMessage(getMessage("LOGGED_OUT_OTHERS").replace("%player%", args[1]));
            }
            
            return true;
        }
        else if (args.length == 0)
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (!p.hasPermission("logit.logout.self"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (!core.getSessionManager().isSessionAlive(p))
            {
                s.sendMessage(getMessage("NOT_LOGGED_IN_SELF"));
                return true;
            }
            
            core.getSessionManager().endSession(p, true);
            
            return true;
        }
        
        s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        
        return true;
    }
    
    private LogItCore core;
}
