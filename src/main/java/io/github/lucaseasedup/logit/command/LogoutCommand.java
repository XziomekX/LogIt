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
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LogoutCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 2)
        {
            if (player != null && !player.hasPermission("logit.logout.others"))
            {
                sendMsg(sender, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "player"));
                
                return true;
            }
            
            Player paramPlayer = Bukkit.getPlayerExact(args[1]);
            
            if (paramPlayer == null)
            {
                sendMsg(sender, _("playerNotOnline")
                        .replace("{0}", args[1]));
                
                return true;
            }
            
            if (!getSessionManager().isSessionAlive(paramPlayer))
            {
                sendMsg(sender, _("notLoggedIn.others")
                        .replace("{0}", paramPlayer.getName()));
                
                return true;
            }
            
            if (!getSessionManager().endSession(paramPlayer).isCancelled())
            {
                sendMsg(paramPlayer, _("endSession.success.self"));
                sendMsg(sender, _("endSession.success.others")
                        .replace("{0}", paramPlayer.getName()));
            }
        }
        else if (args.length == 0)
        {
            if (player == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.logout.self"))
            {
                sendMsg(player, _("noPerms"));
                
                return true;
            }
            
            if (!getSessionManager().isSessionAlive(player))
            {
                sendMsg(player, _("notLoggedIn.self"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                    keys().username(),
                    keys().persistence()
            ));
            
            if (!getSessionManager().endSession(player).isCancelled())
            {
                sendMsg(sender, _("endSession.success.self"));
                
                if (account != null
                        && getConfig("config.yml").getBoolean("login-sessions.enabled"))
                {
                    account.eraseLoginSession();
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
