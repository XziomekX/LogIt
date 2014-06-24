/*
 * UnregisterCommand.java
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
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class UnregisterCommand extends LogItCoreObject implements TabExecutor
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
            if (player != null && !player.hasPermission("logit.unregister.others"))
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
            
            Account account = getAccountManager().selectAccount(args[1], Arrays.asList(
                    keys().username()
            ));
            
            if (account == null)
            {
                sendMsg(sender, _("notRegistered.others")
                        .replace("{0}", args[1]));
                
                return true;
            }
            
            if (player != null && player.getName().equalsIgnoreCase(args[1]))
            {
                sendMsg(sender, _("removeAccount.indirectAccountRemoval"));
                
                return true;
            }
            
            if (PlayerUtils.isPlayerOnline(args[1]))
            {
                Player paramPlayer = PlayerUtils.getPlayer(args[1]);
                
                if (getSessionManager().isSessionAlive(paramPlayer))
                {
                    if (!getSessionManager().endSession(paramPlayer).isCancelled())
                    {
                        sendMsg(paramPlayer, _("endSession.success.self"));
                        sendMsg(sender, _("endSession.success.others")
                                .replace("{0}", paramPlayer.getName()));
                    }
                }
            }
            
            try
            {
                ReportedException.incrementRequestCount();
                
                if (!getAccountManager().removeAccount(args[1]).isCancelled())
                {
                    sendMsg(args[1], _("removeAccount.success.self"));
                    sendMsg(sender, _("removeAccount.success.others")
                            .replace("{0}", PlayerUtils.getPlayerRealName(args[1])));
                }
            }
            catch (ReportedException ex)
            {
                sendMsg(sender, _("removeAccount.fail.others")
                        .replace("{0}", PlayerUtils.getPlayerRealName(args[1])));
            }
            finally
            {
                ReportedException.decrementRequestCount();
            }
        }
        else if ((args.length == 0 && disablePasswords) || (args.length <= 1 && !disablePasswords))
        {
            if (player == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.unregister.self"))
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
            
            if (getCooldownManager().isCooldownActive(player, LogItCooldowns.UNREGISTER))
            {
                getMessageDispatcher().sendCooldownMessage(player.getName(),
                        getCooldownManager().getCooldownMillis(player, LogItCooldowns.UNREGISTER));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                    keys().username(),
                    keys().salt(),
                    keys().password(),
                    keys().hashing_algorithm(),
                    keys().persistence()
            ));
            
            if (account == null)
            {
                sendMsg(player, _("notRegistered.self"));
                
                return true;
            }
            
            if (!disablePasswords && !account.checkPassword(args[0]))
            {
                sendMsg(player, _("incorrectPassword"));
                
                return true;
            }
            
            if (getSessionManager().isSessionAlive(player))
            {
                if (!getSessionManager().endSession(player).isCancelled())
                {
                    sendMsg(sender, _("endSession.success.self"));
                }
            }
            
            try
            {
                ReportedException.incrementRequestCount();
                
                if (!getAccountManager().removeAccount(player.getName()).isCancelled())
                {
                    sendMsg(sender, _("removeAccount.success.self"));
                    
                    LogItCooldowns.activate(LogItCooldowns.UNREGISTER, player);
                }
            }
            catch (ReportedException ex)
            {
                sendMsg(sender, _("removeAccount.fail.self"));
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command cmd,
                                      String label,
                                      String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tab-completion"))
            return null;
        
        if (args.length == 2 && args[0].equals("-x"))
        {
            if (sender instanceof Player
                    && !sender.hasPermission("logit.unregister.others"))
                return null;
            
            return getTabCompleter().completeUsername(args[1]);
        }
        
        return null;
    }
}
