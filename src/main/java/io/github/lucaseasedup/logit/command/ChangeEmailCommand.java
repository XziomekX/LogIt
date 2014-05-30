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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
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
                        .replace("{0}", "newemail"));
            }
            else if (!getAccountManager().isRegistered(args[1]))
            {
                sendMsg(sender, _("notRegistered.others")
                        .replace("{0}", args[1]));
            }
            else if (!EmailUtils.validateEmail(args[2]))
            {
                sendMsg(sender, _("changeEmail.invalidEmailAddress"));
            }
            else
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getAccountManager().changeEmail(args[1], args[2]);
                    
                    sendMsg(args[1], _("changeEmail.success.self")
                            .replace("{0}", args[2]));
                    sendMsg(sender, _("changeEmail.success.others")
                            .replace("{0}", args[1])
                            .replace("{1}", args[2]));
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("changeEmail.fail.others")
                            .replace("{0}", args[1])
                            .replace("{1}", args[2]));
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
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!p.hasPermission("logit.changeemail.self"))
            {
                sendMsg(p, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "newemail"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(p, LogItCooldowns.CHANGEEMAIL))
            {
                getMessageDispatcher().sendCooldownMessage(p.getName(),
                        getCooldownManager().getCooldownMillis(p, LogItCooldowns.CHANGEEMAIL));
                
                return true;
            }
            
            if (!getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(p, _("notRegistered.self"));
                
                return true;
            }
            
            if (!EmailUtils.validateEmail(args[0]))
            {
                sendMsg(p, _("changeEmail.invalidEmailAddress"));
                
                return true;
            }
            
            try
            {
                ReportedException.incrementRequestCount();
                
                getAccountManager().changeEmail(p.getName(), args[0]);
                
                getCooldownManager().activateCooldown(p, LogItCooldowns.CHANGEEMAIL,
                        getConfig().getTime("cooldowns.changeemail", TimeUnit.MILLISECONDS));
                
                sendMsg(sender, _("changeEmail.success.self")
                        .replace("{0}", args[0]));
            }
            catch (ReportedException ex)
            {
                sendMsg(sender, _("changeEmail.fail.self"));
            }
            finally
            {
                ReportedException.decrementRequestCount();
            }
        }
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
}
