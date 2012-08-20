/*
 * ChangePassCommand.java
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
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePassCommand implements CommandExecutor
{
    public ChangePassCommand(LogItCore core)
    {
        this.core = core;
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        if (!cmd.getName().equalsIgnoreCase("changepass"))
            return false;
        
        Player p = null;
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        if ((args.length > 2 && !args[0].equals("-x")) || args.length > 3)
        {
            s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
            return true;
        }
        
        if (args.length > 0 && args[0].equals("-x"))
        {
            if (p != null && !p.hasPermission("logit.changepass.others"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
                return true;
            }
            if (args.length < 3)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "newpassword"));
                return true;
            }
            if (!core.isRegistered(args[1]))
            {
                s.sendMessage(getMessage("NOT_REGISTERED_OTHERS").replace("%player%", args[1]));
                return true;
            }
            if (args[2].length() < core.getConfig().getPasswordMinLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%", String.valueOf(core.getConfig().getPasswordMinLength())));
                return true;
            }
            
            core.changePassword(args[1], args[2], true);
            
            if (p != null)
            {
                p.sendMessage(getMessage("PASSWORD_CHANGED_OTHERS").replace("%player%", args[1]));
            }
        }
        else
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (!p.hasPermission("logit.changepass.self"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (args.length < 1)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "oldpassword"));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "newpassword"));
                return true;
            }
            if (!core.isRegistered(p))
            {
                s.sendMessage(getMessage("NOT_REGISTERED_SELF"));
                return true;
            }
            if (!core.checkPassword(p.getName(), args[0]))
            {
                s.sendMessage(getMessage("INCORRECT_PASSWORD"));
                return true;
            }
            if (args[1].length() < core.getConfig().getPasswordMinLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%", String.valueOf(core.getConfig().getPasswordMinLength())));
                return true;
            }
            
            core.changePassword(p.getName(), args[1], true);
        }
        
        return true;
    }
    
    private LogItCore core;
}