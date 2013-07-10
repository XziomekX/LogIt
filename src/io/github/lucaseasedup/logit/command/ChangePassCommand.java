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
package io.github.lucaseasedup.logit.command;

import io.github.lucaseasedup.logit.LogItCore;
import static io.github.lucaseasedup.logit.util.MessageSender.sendMessage;
import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.logging.Logger;

public class ChangePassCommand extends AbstractCommandExecutor
{
    public ChangePassCommand(LogItCore core)
    {
        super(core);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        try
        {
            p = (Player) sender;
        }
        catch (ClassCastException ex)
        {
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (p != null && !p.hasPermission("logit.changepass.others"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 2)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "newpassword"));
            }
            else if (!core.getAccountManager().isRegistered(args[1]))
            {
                sender.sendMessage(getMessage("CREATE_ACCOUNT_NOT_OTHERS").replace("%player%", args[1]));
            }
            else if (args[2].length() < core.getConfig().getInt("password.min-length"))
            {
                sender.sendMessage(getMessage("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(core.getConfig().getInt("password.min-length"))));
            }
            else if (args[2].length() > core.getConfig().getInt("password.max-length"))
            {
                sender.sendMessage(getMessage("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(core.getConfig().getInt("password.max-length"))));
            }
            else
            {
                try
                {
                    core.getAccountManager().changeAccountPassword(args[1], args[2]);
                    
                    sendMessage(args[1], getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
                    sender.sendMessage(getMessage("CHANGE_PASSWORD_SUCCESS_OTHERS").replace("%player%", args[1]));
                }
                catch (SQLException | UnsupportedOperationException ex)
                {
                    Logger.getLogger(ChangePassCommand.class.getName()).log(Level.WARNING, null, ex);
                    sender.sendMessage(getMessage("CHANGE_PASSWORD_FAIL_OTHERS").replace("%player%", args[1]));
                }
            }
        }
        else if (args.length <= 3)
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.changepass.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "oldpassword"));
            }
            else if (args.length < 2)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "newpassword"));
            }
            else if (args.length < 3)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "confirmpassword"));
            }
            else if (!core.getAccountManager().isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_NOT_SELF"));
            }
            else if (!core.getAccountManager().checkAccountPassword(p.getName(), args[0]))
            {
                p.sendMessage(getMessage("INCORRECT_PASSWORD"));
            }
            else if (args[1].length() < core.getConfig().getInt("password.min-length"))
            {
                p.sendMessage(getMessage("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(core.getConfig().getInt("password.min-length"))));
            }
            else if (args[1].length() > core.getConfig().getInt("password.max-length"))
            {
                p.sendMessage(getMessage("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(core.getConfig().getInt("password.max-length"))));
            }
            else if (!args[1].equals(args[2]))
            {
                p.sendMessage(getMessage("PASSWORDS_DO_NOT_MATCH"));
            }
            else
            {
                try
                {
                    core.getAccountManager().changeAccountPassword(p.getName(), args[1]);
                    sender.sendMessage(getMessage("CHANGE_PASSWORD_SUCCESS_SELF"));
                }
                catch (SQLException | UnsupportedOperationException ex)
                {
                    Logger.getLogger(ChangePassCommand.class.getName()).log(Level.WARNING, null, ex);
                    sender.sendMessage(getMessage("CHANGE_PASSWORD_FAIL_SELF"));
                }
            }
        }
        else
        {
            sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
}