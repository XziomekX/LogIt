/*
 * UnregisterCommand.java
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
import org.bukkit.Bukkit;
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
        
        boolean disablePasswords = getConfig("config.yml")
                .getBoolean("password.disable-passwords");
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 2)
        {
            if (p != null && !p.hasPermission("logit.unregister.others"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "player"));
            }
            else if (!getAccountManager().isRegistered(args[1]))
            {
                sendMsg(sender, _("notRegistered.others")
                        .replace("{0}", args[1]));
            }
            else if (p != null && p.getName().equalsIgnoreCase(args[1]))
            {
                sendMsg(sender, _("removeAccount.indirectAccountRemoval"));
            }
            else
            {
                if (getSessionManager().isSessionAlive(Bukkit.getPlayerExact(args[1])))
                {
                    if (!getSessionManager().endSession(args[1]).isCancelled())
                    {
                        sendMsg(args[1], _("endSession.success.self"));
                        sendMsg(sender, _("endSession.success.others")
                                .replace("{0}", args[1]));
                    }
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().removeAccount(args[1]).isCancelled())
                    {
                        sendMsg(args[1], _("removeAccount.success.self"));
                        sendMsg(sender, _("removeAccount.success.others")
                                .replace("{0}", args[1]));
                    }
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("removeAccount.fail.others")
                            .replace("{0}", args[1]));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if ((args.length == 0 && disablePasswords) || (args.length <= 1 && !disablePasswords))
        {
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!p.hasPermission("logit.unregister.self"))
            {
                sendMsg(p, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 1 && !disablePasswords)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "password"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(p, LogItCooldowns.UNREGISTER))
            {
                getMessageDispatcher().sendCooldownMessage(p.getName(),
                        getCooldownManager().getCooldownMillis(p, LogItCooldowns.UNREGISTER));
                
                return true;
            }
            
            if (!getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(p, _("notRegistered.self"));
                
                return true;
            }
            
            if (!disablePasswords
                    && !getAccountManager().checkAccountPassword(p.getName(), args[0]))
            {
                sendMsg(p, _("incorrectPassword"));
                
                return true;
            }
            
            if (getSessionManager().isSessionAlive(p))
            {
                if (!getSessionManager().endSession(p.getName()).isCancelled())
                {
                    sendMsg(sender, _("endSession.success.self"));
                }
            }
            
            try
            {
                ReportedException.incrementRequestCount();
                
                if (getAccountManager().removeAccount(p.getName()).isCancelled())
                {
                    return true;
                }
                
                long cooldown = getConfig("config.yml")
                        .getTime("cooldowns.unregister", TimeUnit.MILLISECONDS);
                
                getCooldownManager().activateCooldown(p, LogItCooldowns.UNREGISTER, cooldown);
                
                sendMsg(sender, _("removeAccount.success.self"));
            }
            catch (ReportedException ex)
            {
                sendMsg(sender, _("removeAccount.fail.self"));
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
