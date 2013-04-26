/*
 * RegisterCommand.java
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
import static com.gmail.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand extends AbstractCommandExecutor
{
    public RegisterCommand(LogItCore core)
    {
        super(core);
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (p != null && ((core.isPlayerForcedToLogin(p) && !core.getSessionManager().isSessionAlive(p))
                    || !p.hasPermission("logit.register.others")))
            {
                s.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
            }
            else if (args.length < 3)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (core.getAccountManager().isRegistered(args[1]))
            {
                s.sendMessage(getMessage("CREATE_ACCOUNT_ALREADY_OTHERS").replace("%player%", args[1]));
            }
            else if (args[2].length() < core.getConfig().getPasswordMinLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%",
                        String.valueOf(core.getConfig().getPasswordMinLength())));
            }
            else if (args[2].length() > core.getConfig().getPasswordMaxLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_LONG").replace("%max-length%",
                        String.valueOf(core.getConfig().getPasswordMaxLength())));
            }
            else
            {
                core.getAccountManager().createAccount(args[1], args[2]);

                s.sendMessage(getMessage("CREATE_ACCOUNT_SUCCESS_OTHERS").replace("%player%", args[1]));
            }
        }
        else if (args.length <= 2)
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.register.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (args.length < 2)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "confirmpassword"));
            }
            else if (core.getAccountManager().isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_ALREADY_SELF"));
            }
            else if (args[0].length() < core.getConfig().getPasswordMinLength())
            {
                p.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%",
                        String.valueOf(core.getConfig().getPasswordMinLength())));
            }
            else if (args[0].length() > core.getConfig().getPasswordMaxLength())
            {
                p.sendMessage(getMessage("PASSWORD_TOO_LONG").replace("%max-length%",
                        String.valueOf(core.getConfig().getPasswordMaxLength())));
            }
            else if (!args[0].equals(args[1]))
            {
                p.sendMessage(getMessage("PASSWORDS_DO_NOT_MATCH"));
            }
            else if (core.getAccountManager().countAccountsWithIp(getPlayerIp(p)) >= core.getConfig().getAccountsPerIp()
                && !core.getConfig().getUnrestrictedIps().contains(getPlayerIp(p)))
            {
                p.sendMessage(getMessage("ACCOUNTS_PER_IP_LIMIT"));
            }
            else
            {
                core.getAccountManager().createAccount(p.getName(), args[0]);
                core.getAccountManager().attachIp(p.getName(), getPlayerIp(p));
                core.getSessionManager().startSession(p.getName());
            }
        }
        else
        {
            s.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
}
