/*
 * RegisterCommand.java
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
import static io.github.lucaseasedup.logit.util.MessageUtils.sendMessage;
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

/**
 * @author LucasEasedUp
 */
public final class RegisterCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        try
        {
            p = (Player) sender;
        }
        catch (ClassCastException ex)
        {
        }

        int minPasswordLength = getConfig().getInt("password.min-length");
        int maxPasswordLength = getConfig().getInt("password.max-length");
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (p != null && ((getCore().isPlayerForcedToLogIn(p) && !getSessionManager().isSessionAlive(p))
                    || !p.hasPermission("logit.register.others")))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 2)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "player"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (getAccountManager().isRegistered(args[1]))
            {
                sender.sendMessage(getMessage("CREATE_ACCOUNT_ALREADY_OTHERS")
                        .replace("%player%", args[1]));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args[2].length() < minPasswordLength)
            {
                sender.sendMessage(getMessage("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(minPasswordLength)));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args[2].length() > maxPasswordLength)
            {
                sender.sendMessage(getMessage("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(maxPasswordLength)));
            }
            else
            {
                String password = "";
                
                if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password"))
                {
                    password = args[2];
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().createAccount(args[1], password).isCancelled())
                    {
                        sendMessage(args[1], getMessage("CREATE_ACCOUNT_SUCCESS_SELF"));
                        sender.sendMessage(getMessage("CREATE_ACCOUNT_SUCCESS_OTHERS")
                                .replace("%player%", args[1]));
                        
                        if (isPlayerOnline(args[1]))
                        {
                            getAccountManager().attachIp(args[1], getPlayerIp(getPlayer(args[1])));
                            
                            if (!getSessionManager().startSession(args[1]).isCancelled())
                            {
                                sendMessage(args[1], getMessage("START_SESSION_SUCCESS_SELF"));
                                sender.sendMessage(getMessage("START_SESSION_SUCCESS_OTHERS")
                                        .replace("%player%", args[1]));
                            }
                            
                            if (getConfig().getBoolean("waiting-room.enabled")
                                    && getConfig().getBoolean("waiting-room.newbie-teleport.enabled"))
                            {
                                Location newbieTeleportLocation =
                                        getConfig().getLocation("waiting-room.newbie-teleport.location").toBukkitLocation();
                                
                                getPlayer(args[1]).teleport(newbieTeleportLocation);
                            }
                        }
                    }
                }
                catch (ReportedException ex)
                {
                    sender.sendMessage(getMessage("CREATE_ACCOUNT_FAIL_OTHERS")
                            .replace("%player%", args[1]));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if (args.length <= 2)
        {
            final int accountsPerIp = getConfig().getInt("crowd-control.accounts-per-ip.amount");
            final List<String> unrestrictedIps =
                    getConfig().getStringList("crowd-control.accounts-per-ip.unrestricted-ips");
            
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.register.self"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args.length < 2)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "confirmpassword"));
            }
            else if (getAccountManager().isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_ALREADY_SELF"));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args[0].length() < minPasswordLength)
            {
                p.sendMessage(getMessage("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(minPasswordLength)));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && args[0].length() > maxPasswordLength)
            {
                p.sendMessage(getMessage("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(maxPasswordLength)));
            }
            else if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password")
                    && !args[0].equals(args[1]))
            {
                p.sendMessage(getMessage("PASSWORDS_DO_NOT_MATCH"));
            }
            else if (getAccountManager().countAccountsWithIp(getPlayerIp(p)) >= accountsPerIp
                    && !unrestrictedIps.contains(getPlayerIp(p)) && accountsPerIp >= 0)
            {
                p.sendMessage(getMessage("ACCOUNTS_PER_IP_LIMIT"));
            }
            else
            {
                String password = "";
                
                if (!getAccountManager().getTable().isColumnDisabled("logit.accounts.password"))
                {
                    password = args[0];
                }
                
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    if (!getAccountManager().createAccount(p.getName(), password).isCancelled())
                    {
                        sender.sendMessage(getMessage("CREATE_ACCOUNT_SUCCESS_SELF"));
                        
                        getAccountManager().attachIp(p.getName(), getPlayerIp(p));
                        
                        if (!getSessionManager().startSession(p.getName()).isCancelled())
                        {
                            sender.sendMessage(getMessage("START_SESSION_SUCCESS_SELF"));
                        }
                        
                        if (getConfig().getBoolean("waiting-room.enabled")
                                && getConfig().getBoolean("waiting-room.newbie-teleport.enabled"))
                        {
                            Location newbieTeleportLocation =
                                    getConfig().getLocation("waiting-room.newbie-teleport.location").toBukkitLocation();
                            
                            p.teleport(newbieTeleportLocation);
                        }
                    }
                }
                catch (ReportedException ex)
                {
                    sender.sendMessage(getMessage("CREATE_ACCOUNT_FAIL_SELF"));
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else
        {
            sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
}
