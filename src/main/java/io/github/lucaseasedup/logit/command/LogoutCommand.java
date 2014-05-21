/*
 * LogoutCommand.java
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
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.LogItCoreObject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LogoutCommand extends LogItCoreObject implements CommandExecutor
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
            if (p != null && !p.hasPermission("logit.logout.others"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing").replace("{0}", "player"));
            }
            else if (!isPlayerOnline(args[1]))
            {
                sendMsg(sender, _("NOT_ONLINE").replace("%player%", args[1]));
            }
            else if (!getSessionManager().isSessionAlive(getPlayer(args[1])))
            {
                sendMsg(sender, _("NOT_LOGGED_IN_OTHERS")
                        .replace("%player%", args[1]));
            }
            else
            {
                if (!getSessionManager().endSession(args[1]).isCancelled())
                {
                    sendMsg(args[1], _("END_SESSION_SUCCESS_SELF"));
                    sendMsg(sender, _("END_SESSION_SUCCESS_OTHERS")
                            .replace("%player%", args[1]));
                }
            }
        }
        else if (args.length == 0)
        {
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.logout.self"))
            {
                sendMsg(p, _("noPerms"));
            }
            else if (!getSessionManager().isSessionAlive(p))
            {
                sendMsg(p, _("NOT_LOGGED_IN_SELF"));
            }
            else
            {
                if (!getSessionManager().endSession(p.getName()).isCancelled())
                {
                    sendMsg(sender, _("END_SESSION_SUCCESS_SELF"));
                    
                    if (getConfig().getBoolean("login-sessions.enabled"))
                    {
                        getAccountManager().eraseLoginSession(p.getName());
                    }
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
