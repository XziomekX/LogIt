/*
 * LoginCommand.java
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
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import static io.github.lucaseasedup.logit.util.PlayerUtils.sendMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LoginCommand extends LogItCoreObject implements CommandExecutor
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
            if (p != null && ((getCore().isPlayerForcedToLogIn(p) && !getSessionManager().isSessionAlive(p))
                    || !p.hasPermission("logit.login.others")))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 2)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
            }
            else if (!isPlayerOnline(args[1]))
            {
                sender.sendMessage(getMessage("NOT_ONLINE").replace("%player%", args[1]));
            }
            else if (getSessionManager().isSessionAlive(getPlayer(args[1])))
            {
                sender.sendMessage(getMessage("START_SESSION_ALREADY_OTHERS")
                        .replace("%player%", args[1]));
            }
            else
            {
                if (!getSessionManager().startSession(args[1]).isCancelled())
                {
                    sendMessage(args[1], getMessage("START_SESSION_SUCCESS_SELF"));
                    sender.sendMessage(getMessage("START_SESSION_SUCCESS_OTHERS")
                            .replace("%player%", args[1]));
                }
            }
        }
        else if (args.length <= 1)
        {
            String username = null;
            
            if (p != null)
            {
                username = p.getName().toLowerCase();
            }
            
            int failsToKick = getConfig().getInt("crowd-control.login-fails-to-kick");
            int failsToBan = getConfig().getInt("crowd-control.login-fails-to-ban");
            
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.login.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 1
                    && !getAccountManager().getTable().isColumnDisabled("logit.accounts.password"))
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (!getAccountManager().isRegistered(username))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_NOT_SELF"));
            }
            else if (getSessionManager().isSessionAlive(p))
            {
                p.sendMessage(getMessage("START_SESSION_ALREADY_SELF"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && !getAccountManager().checkAccountPassword(username, args[0])
                    && !getCore().checkGlobalPassword(args[0]))
            {
                p.sendMessage(getMessage("INCORRECT_PASSWORD"));
                
                Integer currentFailedLoginsToKick = failedLoginsToKick.get(username);
                Integer currentFailedLoginsToBan = failedLoginsToBan.get(username);
                
                failedLoginsToKick.put(username,
                        (currentFailedLoginsToKick != null) ? currentFailedLoginsToKick + 1 : 1);
                failedLoginsToBan.put(username,
                        (currentFailedLoginsToBan != null) ? currentFailedLoginsToBan + 1 : 1);
                
                if (failedLoginsToBan.get(username) >= failsToBan && failsToBan > 0)
                {
                    Bukkit.banIP(getPlayerIp(p));
                    p.kickPlayer(getMessage("TOO_MANY_LOGIN_FAILS_BAN"));
                    
                    failedLoginsToKick.remove(username);
                    failedLoginsToBan.remove(username);
                }
                else if (failedLoginsToKick.get(username) >= failsToKick && failsToKick > 0)
                {
                    p.kickPlayer(getMessage("TOO_MANY_LOGIN_FAILS_KICK"));
                    
                    failedLoginsToKick.remove(username);
                }
            }
            else
            {
                if (!getSessionManager().startSession(username).isCancelled())
                {
                    failedLoginsToKick.remove(username);
                    failedLoginsToBan.remove(username);
                    
                    sender.sendMessage(getMessage("START_SESSION_SUCCESS_SELF"));
                    
                    if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.remember-login"))
                    {
                        sender.sendMessage(getMessage("REMEMBER_PROMPT"));
                    }
                }
            }
        }
        else
        {
            sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
    
    private final Map<String, Integer> failedLoginsToKick = new HashMap<>();
    private final Map<String, Integer> failedLoginsToBan = new HashMap<>();
}
