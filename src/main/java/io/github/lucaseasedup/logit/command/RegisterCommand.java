/*
 * RegisterCommand.java
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
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RegisterCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        int minPasswordLength = getConfig().getInt("password.min-length");
        int maxPasswordLength = getConfig().getInt("password.max-length");
        boolean disablePasswords = getConfig().getBoolean("password.disable-passwords");
        
        if (args.length > 0 && args[0].equals("-x")
                && ((args.length <= 2 && disablePasswords)
                        || (args.length <= 3 && !disablePasswords)))
        {
            if (p != null && (
                   (getCore().isPlayerForcedToLogIn(p) && !getSessionManager().isSessionAlive(p))
                || !p.hasPermission("logit.register.others")
            ))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing").replace("{0}", "player"));
            }
            else if (!disablePasswords && args.length < 3)
            {
                sendMsg(sender, _("paramMissing").replace("{0}", "password"));
            }
            else if (getAccountManager().isRegistered(args[1]))
            {
                sendMsg(sender, _("alreadyRegistered.others").replace("{0}", args[1]));
            }
            else if (!disablePasswords && args[2].length() < minPasswordLength)
            {
                sendMsg(sender, _("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(minPasswordLength)));
            }
            else if (!disablePasswords && args[2].length() > maxPasswordLength)
            {
                sendMsg(sender, _("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(maxPasswordLength)));
            }
            else
            {
                String password = "";
                
                if (!disablePasswords)
                {
                    password = args[2];
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (getAccountManager().createAccount(args[1], password).isCancelled())
                    {
                        return true;
                    }
                    
                    sendMsg(args[1], _("createAccount.success.self"));
                    sendMsg(sender, _("createAccount.success.others")
                            .replace("{0}", args[1]));
                    
                    if (isPlayerOnline(args[1]))
                    {
                        getAccountManager().attachIp(args[1], getPlayerIp(getPlayer(args[1])));
                        
                        if (!getSessionManager().startSession(args[1]).isCancelled())
                        {
                            sendMsg(args[1], _("START_SESSION_SUCCESS_SELF"));
                            sendMsg(sender, _("START_SESSION_SUCCESS_OTHERS")
                                    .replace("%player%", args[1]));
                        }
                        
                        if (getConfig().getBoolean("waiting-room.enabled")
                                && getConfig().getBoolean("waiting-room.newbie-teleport.enabled"))
                        {
                            Location newbieTeleportLocation =
                                    getConfig().getLocation("waiting-room.newbie-teleport.location")
                                            .toBukkitLocation();
                            
                            getPlayer(args[1]).teleport(newbieTeleportLocation);
                        }
                    }
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("createAccount.fail.others")
                            .replace("{0}", args[1]));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if ((args.length == 0 && disablePasswords) || (args.length <= 2 && !disablePasswords))
        {
            final int accountsPerIp = getConfig().getInt("crowd-control.accounts-per-ip.amount");
            final List<String> unrestrictedIps =
                    getConfig().getStringList("crowd-control.accounts-per-ip.unrestricted-ips");
            
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.register.self"))
            {
                sendMsg(p, _("noPerms"));
            }
            else if (!disablePasswords && args.length < 1)
            {
                sendMsg(p, _("paramMissing").replace("{0}", "password"));
            }
            else if (!disablePasswords && args.length < 2)
            {
                sendMsg(p, _("paramMissing").replace("{0}", "confirmpassword"));
            }
            else if (getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(p, _("alreadyRegistered.self"));
            }
            else if (!disablePasswords && args[0].length() < minPasswordLength)
            {
                sendMsg(p, _("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(minPasswordLength)));
            }
            else if (!disablePasswords && args[0].length() > maxPasswordLength)
            {
                sendMsg(p, _("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(maxPasswordLength)));
            }
            else if (!disablePasswords && !args[0].equals(args[1]))
            {
                sendMsg(p, _("PASSWORDS_DO_NOT_MATCH"));
            }
            else if (getAccountManager().countAccountsWithIp(getPlayerIp(p)) >= accountsPerIp
                    && !unrestrictedIps.contains(getPlayerIp(p)) && accountsPerIp >= 0)
            {
                sendMsg(p, _("ACCOUNTS_PER_IP_LIMIT"));
            }
            else
            {
                String password = "";
                
                if (!disablePasswords)
                {
                    password = args[0];
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (getAccountManager().createAccount(p.getName(), password).isCancelled())
                    {
                        return true;
                    }
                    
                    sendMsg(sender, _("createAccount.success.self"));
                    
                    getAccountManager().attachIp(p.getName(), getPlayerIp(p));
                    
                    if (!getSessionManager().startSession(p.getName()).isCancelled())
                    {
                        sendMsg(sender, _("START_SESSION_SUCCESS_SELF"));
                    }
                    
                    if (getConfig().getBoolean("waiting-room.enabled")
                            && getConfig().getBoolean("waiting-room.newbie-teleport.enabled"))
                    {
                        Location newbieTeleportLocation =
                                getConfig().getLocation("waiting-room.newbie-teleport.location")
                                        .toBukkitLocation();
                        
                        p.teleport(newbieTeleportLocation);
                    }
                    
                    if (getConfig().getBoolean("login-sessions.enabled"))
                    {
                        sendMsg(sender, _("REMEMBER_PROMPT"));
                    }
                    
                    if (getConfig().getBoolean("password-recovery.prompt-to-add-email")
                            && getConfig().getBoolean("password-recovery.enabled"))
                    {
                        sendMsg(sender, _("noEmailSet"));
                    }
                }
                catch (ReportedException ex)
                {
                    sendMsg(sender, _("createAccount.fail.self"));
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
