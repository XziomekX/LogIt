/*
 * UnregisterCommand.java
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

public class UnregisterCommand implements CommandExecutor
{
    public UnregisterCommand(LogItCore core)
    {
        this.core = core;
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        if (!cmd.getName().equalsIgnoreCase("unregister"))
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
            if (p != null && !p.hasPermission("logit.unregister.others"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
                return true;
            }
            if (!core.getAccountManager().isAccountCreated(args[1]))
            {
                s.sendMessage(getMessage("CREATE_ACCOUNT_NOT_OTHERS").replace("%player%", args[1]));
                return true;
            }
            if (p != null && p.getName().equalsIgnoreCase(args[1]))
            {
                s.sendMessage(getMessage("REMOVE_ACCOUNT_INDIRECT_SELF"));
                return true;
            }
            
            core.getAccountManager().removeAccount(args[1]);
            
            s.sendMessage(getMessage("REMOVE_ACCOUNT_SUCCESS_OTHERS").replace("%player%", args[1]));
            
            return true;
        }
        else if (args.length <= 1)
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (!p.hasPermission("logit.unregister.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
                return true;
            }
            if (!core.getAccountManager().isAccountCreated(p.getName()))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_NOT_SELF"));
                return true;
            }
            if (!core.getAccountManager().checkAccountPassword(p.getName(), args[0]))
            {
                p.sendMessage(getMessage("INCORRECT_PASSWORD"));
                return true;
            }
            
            core.getSessionManager().endSession(p);
            core.getAccountManager().removeAccount(p.getName());
            
            return true;
        }
        
        s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        
        return true;
    }
    
    private final LogItCore core;
}
