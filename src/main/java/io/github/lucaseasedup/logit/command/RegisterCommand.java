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
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.Storage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
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
        
        int minPasswordLength = getConfig("config.yml").getInt("password.min-length");
        int maxPasswordLength = getConfig("config.yml").getInt("password.max-length");
        
        boolean disablePasswords = getConfig("config.yml")
                .getBoolean("password.disable-passwords");
        
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
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "player"));
            }
            else if (!disablePasswords && args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "password"));
            }
            else if (getAccountManager().isRegistered(args[1]))
            {
                sendMsg(sender, _("alreadyRegistered.others")
                        .replace("{0}", args[1]));
            }
            else if (!disablePasswords && args[2].length() < minPasswordLength)
            {
                sendMsg(sender, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
            }
            else if (!disablePasswords && args[2].length() > maxPasswordLength)
            {
                sendMsg(sender, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
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
                        getAccountManager().attachIp(args[1],
                                getPlayerIp(Bukkit.getPlayerExact(args[1])));
                        
                        if (!getSessionManager().startSession(args[1]).isCancelled())
                        {
                            sendMsg(args[1], _("startSession.success.self"));
                            sendMsg(sender, _("startSession.success.others")
                                    .replace("{0}", args[1]));
                        }
                        
                        boolean newbieTeleportEnabled = getConfig("config.yml")
                                .getBoolean("waiting-room.newbie-teleport.enabled");
                        
                        if (getConfig("config.yml").getBoolean("waiting-room.enabled")
                                && newbieTeleportEnabled)
                        {
                            Location newbieTeleportLocation = getConfig("config.yml")
                                    .getLocation("waiting-room.newbie-teleport.location")
                                    .toBukkitLocation();
                            
                            Bukkit.getPlayerExact(args[1]).teleport(newbieTeleportLocation);
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
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!p.hasPermission("logit.register.self"))
            {
                sendMsg(p, _("noPerms"));
                
                return true;
            }
            
            if (!disablePasswords && args.length < 1)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "password"));
                
                return true;
            }
            
            if (!disablePasswords && args.length < 2)
            {
                sendMsg(p, _("paramMissing")
                        .replace("{0}", "confirmpassword"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(p, LogItCooldowns.REGISTER))
            {
                getMessageDispatcher().sendCooldownMessage(p.getName(),
                        getCooldownManager().getCooldownMillis(p, LogItCooldowns.REGISTER));
                
                return true;
            }
            
            if (getAccountManager().isRegistered(p.getName()))
            {
                sendMsg(p, _("alreadyRegistered.self"));
                
                return true;
            }
            
            if (!disablePasswords && args[0].length() < minPasswordLength)
            {
                sendMsg(p, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
                
                return true;
            }
            
            if (!disablePasswords && args[0].length() > maxPasswordLength)
            {
                sendMsg(p, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
                
                return true;
            }
            
            if (!disablePasswords && !args[0].equals(args[1]))
            {
                sendMsg(p, _("passwordsDoNotMatch"));
                
                return true;
            }
            
            final int accountsPerIp = getConfig("config.yml").getInt("accounts-per-ip.amount");
            final List<String> unrestrictedIps =
                    getConfig("config.yml").getStringList("accounts-per-ip.unrestricted-ips");
            
            if (getAccountManager().countAccountsWithIp(getPlayerIp(p)) >= accountsPerIp
                    && !unrestrictedIps.contains(getPlayerIp(p)) && accountsPerIp >= 0)
            {
                sendMsg(p, _("accountsPerIpLimitReached"));
                
                return true;
            }
            
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
                
                long cooldown = getConfig("config.yml")
                        .getTime("cooldowns.register", TimeUnit.MILLISECONDS);
                
                getCooldownManager().activateCooldown(p, LogItCooldowns.REGISTER, cooldown);
                
                sendMsg(sender, _("createAccount.success.self"));
                
                AccountKeys keys = getAccountManager().getKeys();
                
                getAccountStorage().updateEntries(getAccountManager().getUnit(),
                        new Storage.Entry.Builder()
                                .put(keys.display_name(), p.getName())
                                .build(),
                        new SelectorCondition(
                                keys.username(), Infix.EQUALS, p.getName().toLowerCase()
                        )
                );
                
                getAccountManager().attachIp(p.getName(), getPlayerIp(p));
                
                if (!getSessionManager().startSession(p.getName()).isCancelled())
                {
                    sendMsg(sender, _("startSession.success.self"));
                }
                
                boolean newbieTeleportEnabled = getConfig("config.yml")
                        .getBoolean("waiting-room.newbie-teleport.enabled");
                
                if (getConfig("config.yml").getBoolean("waiting-room.enabled")
                        && newbieTeleportEnabled)
                {
                    Location newbieTeleportLocation = getConfig("config.yml")
                            .getLocation("waiting-room.newbie-teleport.location")
                            .toBukkitLocation();
                    
                    p.teleport(newbieTeleportLocation);
                }
                
                if (getConfig("config.yml").getBoolean("login-sessions.enabled"))
                {
                    sendMsg(sender, _("rememberLogin.prompt"));
                }
                
                if (getConfig("config.yml").getBoolean("password-recovery.prompt-to-add-email")
                        && getConfig("config.yml").getBoolean("password-recovery.enabled"))
                {
                    sendMsg(sender, _("noEmailSet"));
                }
            }
            catch (IOException ex)
            {
                log(Level.WARNING, ex);
                
                sendMsg(sender, _("createAccount.fail.self"));
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
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
}
