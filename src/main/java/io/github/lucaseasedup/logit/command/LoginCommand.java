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
import io.github.lucaseasedup.logit.account.Account;
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
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        boolean disablePasswords = getConfig("config.yml")
                .getBoolean("password.disable-passwords");
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 2)
        {
            if (player != null && (
                   (getCore().isPlayerForcedToLogIn(player)
                           && !getSessionManager().isSessionAlive(player))
                   || !player.hasPermission("logit.login.others")
            ))
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
            
            if (!isPlayerOnline(args[1]))
            {
                sendMsg(sender, _("playerNotOnline")
                        .replace("{0}", args[1]));
                
                return true;
            }
            
            Player paramPlayer = Bukkit.getPlayerExact(args[1]);
            
            if (getSessionManager().isSessionAlive(paramPlayer))
            {
                sendMsg(sender, _("alreadyLoggedIn.others")
                        .replace("{0}", paramPlayer.getName()));
                
                return true;
            }
            
            if (getSessionManager().getSession(paramPlayer) == null)
            {
                getSessionManager().createSession(paramPlayer);
            }
            
            if (!getSessionManager().startSession(paramPlayer).isCancelled())
            {
                sendMsg(paramPlayer, _("startSession.success.self"));
                sendMsg(sender, _("startSession.success.others")
                        .replace("{0}", paramPlayer.getName()));
                
                getConfig("stats.yml").set("logins",
                        getConfig("stats.yml").getInt("logins") + 1);
            }
        }
        else if ((args.length == 0 && disablePasswords) || (args.length <= 1 && !disablePasswords))
        {
            int failsToKick = getConfig("config.yml").getInt("login-fails-to-kick");
            int failsToBan = getConfig("config.yml").getInt("login-fails-to-ban");
            
            if (player == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.login.self"))
            {
                sendMsg(player, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 1 && !disablePasswords)
            {
                sendMsg(player, _("paramMissing")
                        .replace("{0}", "password"));
                
                return true;
            }
            
            if (getSessionManager().isSessionAlive(player))
            {
                sendMsg(player, _("alreadyLoggedIn.self"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                    keys().username(),
                    keys().salt(),
                    keys().password(),
                    keys().hashing_algorithm(),
                    keys().ip(),
                    keys().login_history(),
                    keys().persistence()
            ));
            
            if (account == null)
            {
                sendMsg(player, _("notRegistered.self"));
                
                return true;
            }
            
            String username = player.getName().toLowerCase();
            String playerIp = getPlayerIp(player);
            
            long currentTimeSecs = System.currentTimeMillis() / 1000L;
            
            if (!disablePasswords && !getGlobalPasswordManager().checkPassword(args[0]))
            {
                if (!account.checkPassword(args[0]))
                {
                    sendMsg(player, _("incorrectPassword"));
                    
                    Integer currentFailedLoginsToKick = failedLoginsToKick.get(username);
                    Integer currentFailedLoginsToBan = failedLoginsToBan.get(username);
                    
                    failedLoginsToKick.put(username,
                            currentFailedLoginsToKick != null ? currentFailedLoginsToKick + 1 : 1);
                    failedLoginsToBan.put(username,
                            currentFailedLoginsToBan != null ? currentFailedLoginsToBan + 1 : 1);
                    
                    if (failedLoginsToBan.get(username) >= failsToBan && failsToBan > 0)
                    {
                        Bukkit.banIP(playerIp);
                        player.kickPlayer(_("tooManyLoginFails.ban"));
                        
                        failedLoginsToKick.remove(username);
                        failedLoginsToBan.remove(username);
                    }
                    else if (failedLoginsToKick.get(username) >= failsToKick && failsToKick > 0)
                    {
                        player.kickPlayer(_("tooManyLoginFails.kick"));
                        
                        failedLoginsToKick.remove(username);
                    }
                    
                    if (getConfig("config.yml").getBoolean("login-history.enabled"))
                    {
                        account.recordLogin(currentTimeSecs, playerIp, false);
                    }
                    
                    return true;
                }
            }
            
            if (getSessionManager().getSession(player) == null)
            {
                getSessionManager().createSession(player);
            }
            
            if (!getSessionManager().startSession(player).isCancelled())
            {
                failedLoginsToKick.remove(username);
                failedLoginsToBan.remove(username);
                
                sendMsg(sender, _("startSession.success.self"));
                
                getConfig("stats.yml").set("logins", getConfig("stats.yml").getInt("logins") + 1);
                
                if (getConfig("config.yml").getBoolean("login-sessions.enabled"))
                {
                    sendMsg(sender, _("rememberLogin.prompt"));
                }
                
                if (getConfig("config.yml").getBoolean("login-history.enabled"))
                {
                    account.recordLogin(currentTimeSecs, playerIp, true);
                }
                
                if (account.getIp().trim().isEmpty())
                {
                    account.setIp(playerIp);
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
