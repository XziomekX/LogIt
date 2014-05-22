/*
 * ChangePassCommand.java
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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChangePassCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        int minPasswordLength = getConfig().getInt("password.min-length");
        int maxPasswordLength = getConfig().getInt("password.max-length");
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (p != null && !p.hasPermission("logit.changepass.others"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "player"));
            }
            else if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "newpassword"));
            }
            else if (!getAccountManager().isRegistered(args[1]))
            {
                sendMsg(sender, _("notRegistered.others")
                        .replace("{0}", args[1]));
            }
            else if (args[2].length() < minPasswordLength)
            {
                sendMsg(sender, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
            }
            else if (args[2].length() > maxPasswordLength)
            {
                sendMsg(sender, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
            }
            else
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getAccountManager().changeAccountPassword(args[1], args[2]);
                    
                    sendMsg(args[1], _("changePassword.success.self"));
                    sendMsg(sender, _("changePassword.success.others")
                            .replace("{0}", args[1]));
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("changePassword.fail.others")
                            .replace("{0}", args[1]));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if (args.length <= 3)
        {
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.changepass.self"))
            {
                sendMsg(p, _("noPerms"));
            }
            else if (args.length < 1)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "oldpassword"));
            }
            else if (args.length < 2)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "newpassword"));
            }
            else if (args.length < 3)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "confirmpassword"));
            }
            else if (!getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(p, _("notRegistered.self"));
            }
            else if (!getAccountManager().checkAccountPassword(p.getName(), args[0]))
            {
                sendMsg(p, _("incorrectPassword"));
            }
            else if (args[1].length() < minPasswordLength)
            {
                sendMsg(p, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
            }
            else if (args[1].length() > maxPasswordLength)
            {
                sendMsg(p, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
            }
            else if (!args[1].equals(args[2]))
            {
                sendMsg(p, _("passwordsDoNotMatch"));
            }
            else
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getAccountManager().changeAccountPassword(p.getName(), args[1]);
                    
                    sendMsg(sender, _("changePassword.success.self"));
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("changePassword.fail.self"));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
}
