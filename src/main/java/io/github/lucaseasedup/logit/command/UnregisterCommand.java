/*
 * UnregisterCommand.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
import static io.github.lucaseasedup.logit.util.PlayerUtils.sendMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class UnregisterCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 2)
        {
            if (p != null && !p.hasPermission("logit.unregister.others"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 2)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
            }
            else if (!getAccountManager().isRegistered(args[1]))
            {
                sender.sendMessage(getMessage("CREATE_ACCOUNT_NOT_OTHERS")
                        .replace("%player%", args[1]));
            }
            else if (p != null && p.getName().equalsIgnoreCase(args[1]))
            {
                sender.sendMessage(getMessage("REMOVE_ACCOUNT_INDIRECT_SELF"));
            }
            else
            {
                if (getSessionManager().isSessionAlive(getPlayer(args[1])))
                {
                    if (!getSessionManager().endSession(args[1]).isCancelled())
                    {
                        sendMessage(args[1], getMessage("END_SESSION_SUCCESS_SELF"));
                        sender.sendMessage(getMessage("END_SESSION_SUCCESS_OTHERS")
                                .replace("%player%", args[1]));
                    }
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().removeAccount(args[1]).isCancelled())
                    {
                        sendMessage(args[1], getMessage("REMOVE_ACCOUNT_SUCCESS_SELF"));
                        sender.sendMessage(getMessage("REMOVE_ACCOUNT_SUCCESS_OTHERS")
                                .replace("%player%", args[1]));
                    }
                }
                catch (ReportedException ex)
                {
                    sender.sendMessage(getMessage("REMOVE_ACCOUNT_FAIL_OTHERS")
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
            else if (!p.hasPermission("logit.unregister.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (!getAccountManager().isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_NOT_SELF"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && !getAccountManager().checkAccountPassword(p.getName(), args[0]))
            {
                p.sendMessage(getMessage("INCORRECT_PASSWORD"));
            }
            else
            {
                if (getSessionManager().isSessionAlive(p))
                {
                    if (!getSessionManager().endSession(p.getName()).isCancelled())
                    {
                        sender.sendMessage(getMessage("END_SESSION_SUCCESS_SELF"));
                    }
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().removeAccount(p.getName()).isCancelled())
                    {
                        sender.sendMessage(getMessage("REMOVE_ACCOUNT_SUCCESS_SELF"));
                    }
                }
                catch (ReportedException ex)
                {
                    sender.sendMessage(getMessage("REMOVE_ACCOUNT_FAIL_SELF"));
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
