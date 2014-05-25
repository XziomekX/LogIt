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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.security.HashingAlgorithm;
import io.github.lucaseasedup.logit.storage.Storage;
import java.util.Arrays;
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
        
        boolean disablePasswords = getConfig().getBoolean("password.disable-passwords");
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 2)
        {
            if (p != null && (
                   (getCore().isPlayerForcedToLogIn(p) && !getSessionManager().isSessionAlive(p))
                || !p.hasPermission("logit.login.others")
            ))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "player"));
            }
            else if (!isPlayerOnline(args[1]))
            {
                sendMsg(sender, _("playerNotOnline")
                        .replace("{0}", args[1]));
            }
            else if (getSessionManager().isSessionAlive(Bukkit.getPlayerExact(args[1])))
            {
                sendMsg(sender, _("alreadyLoggedIn.others")
                        .replace("{0}", args[1]));
            }
            else
            {
                Player argPlayer = Bukkit.getPlayerExact(args[1]);
                
                assert argPlayer != null;
                
                if (getSessionManager().getSession(args[1]) == null)
                {
                    getSessionManager().createSession(argPlayer);
                }
                
                if (!getSessionManager().startSession(args[1]).isCancelled())
                {
                    sendMsg(args[1], _("startSession.success.self"));
                    sendMsg(sender, _("startSession.success.others")
                            .replace("{0}", args[1]));
                    
                    getCore().getStats().set("logins", getCore().getStats().getInt("logins") + 1);
                }
            }
        }
        else if ((args.length == 0 && disablePasswords) || (args.length <= 1 && !disablePasswords))
        {
            int failsToKick = getConfig().getInt("crowd-control.login-fails-to-kick");
            int failsToBan = getConfig().getInt("crowd-control.login-fails-to-ban");
            
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!p.hasPermission("logit.login.self"))
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
            
            if (getSessionManager().isSessionAlive(p))
            {
                sendMsg(p, _("alreadyLoggedIn.self"));
                
                return true;
            }
            
            String username = p.getName().toLowerCase();
            AccountKeys keys = getAccountManager().getKeys();
            Storage.Entry accountData = getAccountManager().queryAccount(username, Arrays.asList(
                    keys.username(),
                    keys.salt(),
                    keys.password(),
                    keys.hashing_algorithm(),
                    keys.ip()
                ));
            
            if (accountData == null)
            {
                sendMsg(p, _("notRegistered.self"));
                
                return true;
            }
            
            if (!disablePasswords && !getCore().getGlobalPasswordManager().checkPassword(args[0]))
            {
                String userAlgorithm = accountData.get(keys.hashing_algorithm());
                String hashedPassword = accountData.get(keys.password());
                HashingAlgorithm algorithm = HashingAlgorithm.decode(userAlgorithm);
                String actualSalt = accountData.get(keys.salt());
                
                if (!getCore().checkPassword(args[0], hashedPassword, actualSalt, algorithm))
                {
                    sendMsg(p, _("incorrectPassword"));
                    
                    Integer currentFailedLoginsToKick = failedLoginsToKick.get(username);
                    Integer currentFailedLoginsToBan = failedLoginsToBan.get(username);
                    
                    failedLoginsToKick.put(username,
                            currentFailedLoginsToKick != null ? currentFailedLoginsToKick + 1 : 1);
                    failedLoginsToBan.put(username,
                            currentFailedLoginsToBan != null ? currentFailedLoginsToBan + 1 : 1);
                    
                    if (failedLoginsToBan.get(username) >= failsToBan && failsToBan > 0)
                    {
                        Bukkit.banIP(getPlayerIp(p));
                        p.kickPlayer(_("tooManyLoginFails.ban"));
                        
                        failedLoginsToKick.remove(username);
                        failedLoginsToBan.remove(username);
                    }
                    else if (failedLoginsToKick.get(username) >= failsToKick && failsToKick > 0)
                    {
                        p.kickPlayer(_("tooManyLoginFails.kick"));
                        
                        failedLoginsToKick.remove(username);
                    }
                    
                    return true;
                }
            }
            
            if (getSessionManager().getSession(username) == null)
            {
                getSessionManager().createSession(p);
            }
            
            if (!getSessionManager().startSession(username).isCancelled())
            {
                failedLoginsToKick.remove(username);
                failedLoginsToBan.remove(username);
                
                sendMsg(sender, _("startSession.success.self"));
                
                getCore().getStats().set("logins", getCore().getStats().getInt("logins") + 1);
                
                if (getConfig().getBoolean("login-sessions.enabled"))
                {
                    sendMsg(sender, _("rememberLogin.prompt"));
                }
                
                if (accountData.get(keys.ip()).trim().isEmpty())
                {
                    getAccountManager().attachIp(username, getPlayerIp(p));
                }
            }
        }
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
    
    private final Map<String, Integer> failedLoginsToKick = new HashMap<>();
    private final Map<String, Integer> failedLoginsToBan = new HashMap<>();
}
