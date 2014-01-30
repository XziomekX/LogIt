/*
 * ChangeEmailCommand.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.sendMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.util.EmailUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChangeEmailCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (p != null && !p.hasPermission("logit.changeemail.others"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 2)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "newemail"));
            }
            else if (!getAccountManager().isRegistered(args[1]))
            {
                sender.sendMessage(getMessage("NOT_REGISTERED_OTHERS")
                        .replace("%player%", args[1]));
            }
            else if (!EmailUtils.validateEmail(args[2]))
            {
                sender.sendMessage(getMessage("INVALID_EMAIL_ADDRESS"));
            }
            else
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().changeEmail(args[1], args[2]).isCancelled())
                    {
                        sendMessage(args[1], getMessage("CHANGE_EMAIL_SUCCESS_SELF")
                                .replace("%email%", args[2]));
                        sender.sendMessage(getMessage("CHANGE_EMAIL_SUCCESS_OTHERS")
                                .replace("%player%", args[1]));
                    }
                }
                catch (ReportedException ex)
                {
                    sender.sendMessage(getMessage("CHANGE_EMAIL_FAIL_OTHERS")
                            .replace("%player%", args[1]));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if (args.length <= 1)
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.changeemail.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "newemail"));
            }
            else if (!getAccountManager().isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("NOT_REGISTERED_SELF"));
            }
            else if (!EmailUtils.validateEmail(args[0]))
            {
                p.sendMessage(getMessage("INVALID_EMAIL_ADDRESS"));
            }
            else
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().changeEmail(p.getName(), args[0]).isCancelled())
                    {
                        sender.sendMessage(getMessage("CHANGE_EMAIL_SUCCESS_SELF")
                                .replace("%email%", args[0]));
                    }
                }
                catch (ReportedException ex)
                {
                    sender.sendMessage(getMessage("CHANGE_EMAIL_FAIL_SELF"));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
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
