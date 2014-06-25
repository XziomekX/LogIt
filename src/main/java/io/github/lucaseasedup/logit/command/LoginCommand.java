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
import io.github.lucaseasedup.logit.PlayerCollections;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.locale.Locale;
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
            
            if (loginBlockade.containsKey(player))
            {
                long blockadeExpirationTimeMillis = loginBlockade.get(player);
                Locale locale = getLocaleManager().getActiveLocale();
                
                if (blockadeExpirationTimeMillis - 1000L > System.currentTimeMillis())
                {
                    long blockageTimeSecs = TimeUnit.MILLISECONDS.convert(
                            blockadeExpirationTimeMillis - System.currentTimeMillis(),
                            TimeUnit.SECONDS
                    );
                    
                    sendMsg(player, _("tooManyLoginFails.blockLoggingIn")
                            .replace("{0}", locale.stringifySeconds(blockageTimeSecs)));
                    
                    return true;
                }
                
                loginBlockade.remove(player);
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
            
            String playerIp = getPlayerIp(player);
            
            long currentTimeSecs = System.currentTimeMillis() / 1000L;
            
            if (!disablePasswords && !getGlobalPasswordManager().checkPassword(args[0]))
            {
                if (!account.checkPassword(args[0]))
                {
                    sendMsg(player, _("incorrectPassword"));
                    
                    int failsToBlockLoggingIn = getConfig("config.yml")
                            .getInt("brute-force-prevention"
                                  + ".block-logging-in"
                                  + ".login-attempts");
                    
                    int failsToKick = getConfig("config.yml")
                            .getInt("brute-force-prevention"
                                  + ".kick"
                                  + ".login-attempts");
                    
                    int failsToBan = getConfig("config.yml")
                            .getInt("brute-force-prevention"
                                  + ".ban"
                                  + ".login-attempts");
                    
                    Integer currentFailedLogins = failedLogins.get(player);
                    
                    failedLogins.put(player,
                            currentFailedLogins != null ? currentFailedLogins + 1 : 1);
                    
                    if (failsToBan > 0 && failedLogins.get(player) >= failsToBan)
                    {
                        Bukkit.banIP(playerIp);
                        
                        player.kickPlayer(_("tooManyLoginFails.ban"));
                        
                        failedLogins.remove(player);
                    }
                    else if (failsToKick > 0 && failedLogins.get(player) >= failsToKick)
                    {
                        player.kickPlayer(_("tooManyLoginFails.kick"));
                        
                        failedLogins.remove(player);
                    }
                    else if (failsToBlockLoggingIn > 0
                            && failedLogins.get(player) >= failsToBlockLoggingIn)
                    {
                        long loginBlockadeTimeMillis = getConfig("config.yml")
                                .getTime("brute-force-prevention"
                                       + ".block-logging-in"
                                       + ".for-time", TimeUnit.MILLISECONDS);
                        
                        long loginBlockadeTimeSecs =
                                TimeUnit.MILLISECONDS.convert(loginBlockadeTimeMillis,
                                        TimeUnit.SECONDS);
                        
                        Locale locale = getLocaleManager().getActiveLocale();
                        
                        loginBlockade.put(player,
                                System.currentTimeMillis() + loginBlockadeTimeMillis);
                        
                        sendMsg(player, _("tooManyLoginFails.blockLoggingIn")
                                .replace("{0}", locale.stringifySeconds(loginBlockadeTimeSecs)));
                        
                        failedLogins.remove(player);
                    }
                    
                    if (getConfig("config.yml").getBoolean("login-history.enabled"))
                    {
                        account.recordLogin(currentTimeSecs, playerIp, false);
                    }
                    
                    return true;
                }
            }
            
            failedLogins.remove(player);
            
            if (getSessionManager().getSession(player) == null)
            {
                getSessionManager().createSession(player);
            }
            
            if (!getSessionManager().startSession(player).isCancelled())
            {
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
    
    private final Map<Player, Integer> failedLogins =
            PlayerCollections.monitoredMap(new HashMap<Player, Integer>());
    private final Map<Player, Long> loginBlockade =
            PlayerCollections.monitoredMap(new HashMap<Player, Long>());
}
