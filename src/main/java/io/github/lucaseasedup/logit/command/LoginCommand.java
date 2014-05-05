/*
 * LoginCommand.java
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
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayer;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import static io.github.lucaseasedup.logit.util.PlayerUtils.sendMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.security.HashingAlgorithm;
import io.github.lucaseasedup.logit.storage.Storage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
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
        
        boolean disablePasswords = getConfig().getBoolean("password.disable-passwords");
        
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
                sender.sendMessage(getMessage("ALREADY_LOGGED_IN_OTHERS")
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
        else if ((args.length == 0 && disablePasswords) || (args.length <= 1 && !disablePasswords))
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
                
                return true;
            }
            
            if (!p.hasPermission("logit.login.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
                
                return true;
            }
            
            if (args.length < 1 && !disablePasswords)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
                
                return true;
            }
            
            if (getSessionManager().isSessionAlive(p))
            {
                p.sendMessage(getMessage("ALREADY_LOGGED_IN_SELF"));
                
                return true;
            }
            
            AccountKeys keys = getAccountManager().getKeys();
            Storage.Entry accountData;
            
            try
            {
                accountData = getAccountManager().queryAccount(username);
            }
            catch (IOException ex)
            {
                log(Level.WARNING, ex);
                
                sender.sendMessage(getMessage("START_SESSION_FAIL_SELF"));
                
                return true;
            }
            
            if (accountData == null)
            {
                p.sendMessage(getMessage("NOT_REGISTERED_SELF"));
                
                return true;
            }
            
            if (!disablePasswords && !getCore().checkGlobalPassword(args[0]))
            {
                String userAlgorithm = accountData.get(keys.hashing_algorithm());
                String hashedPassword = accountData.get(keys.password());
                HashingAlgorithm algorithm = HashingAlgorithm.decode(userAlgorithm);
                String actualSalt = accountData.get(keys.salt());
                
                if (!getCore().checkPassword(args[0], hashedPassword, actualSalt, algorithm))
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
                    
                    return true;
                }
            }
            
            if (!getSessionManager().startSession(username).isCancelled())
            {
                failedLoginsToKick.remove(username);
                failedLoginsToBan.remove(username);
                
                sender.sendMessage(getMessage("START_SESSION_SUCCESS_SELF"));
                
                if (getConfig().getBoolean("login-sessions.enabled"))
                {
                    sender.sendMessage(getMessage("REMEMBER_PROMPT"));
                }
                
                if (accountData.get(keys.ip()).trim().isEmpty())
                {
                    getAccountManager().attachIp(username, getPlayerIp(p));
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
