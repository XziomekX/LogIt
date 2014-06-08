/*
 * RememberCommand.java
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
import io.github.lucaseasedup.logit.locale.Locale;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RememberCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        if (args.length == 0)
        {
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.remember"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (!getSessionManager().isSessionAlive(p))
            {
                sendMsg(sender, _("notLoggedIn.self"));
            }
            else if (!getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(sender, _("notRegistered.self"));
            }
            else
            {
                long validnessTime = getConfig("config.yml")
                        .getTime("login-sessions.validness-time", TimeUnit.SECONDS);
                
                int currentTime = (int) (System.currentTimeMillis() / 1000L);
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getAccountManager().saveLoginSession(p.getName(),
                            PlayerUtils.getPlayerIp(p), currentTime);
                    
                    Locale activeLocale = getLocaleManager().getActiveLocale();
                    
                    sendMsg(sender, _("rememberLogin.success")
                            .replace("{0}", activeLocale.stringifySeconds((int) validnessTime)));
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("rememberLogin.fail"));
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
