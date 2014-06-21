/*
 * ChangeEmailCommand.java
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
import io.github.lucaseasedup.logit.cooldown.LogItCooldowns;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.util.EmailUtils;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChangeEmailCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (player != null && !player.hasPermission("logit.changeemail.others"))
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
            
            if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "newemail"));
                
                return true;
            }
            
            if (!EmailUtils.validateEmail(args[2]))
            {
                sendMsg(sender, _("changeEmail.invalidEmailAddress"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(args[1], Arrays.asList(
                    keys().username()
            ));
            
            if (account == null)
            {
                sendMsg(sender, _("notRegistered.others")
                        .replace("{0}", PlayerUtils.getPlayerRealName(args[1])));
                
                return true;
            }
            
            account.setEmail(args[2]);
            
            sendMsg(args[1], _("changeEmail.success.self")
                    .replace("{0}", args[2].toLowerCase()));
            sendMsg(sender, _("changeEmail.success.others")
                    .replace("{0}", PlayerUtils.getPlayerRealName(args[1]))
                    .replace("{1}", args[2].toLowerCase()));
        }
        else if (args.length <= 1)
        {
            if (player == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.changeemail.self"))
            {
                sendMsg(player, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(player, _("paramMissing")
                        .replace("{0}", "newemail"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(player, LogItCooldowns.CHANGEEMAIL))
            {
                getMessageDispatcher().sendCooldownMessage(player.getName(),
                        getCooldownManager().getCooldownMillis(player, LogItCooldowns.CHANGEEMAIL));
                
                return true;
            }
            
            if (!EmailUtils.validateEmail(args[0]))
            {
                sendMsg(player, _("changeEmail.invalidEmailAddress"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                    keys().username()
            ));
            
            if (account == null)
            {
                sendMsg(player, _("notRegistered.self"));
                
                return true;
            }
            
            int accountsWithEmail = getAccountManager().selectAccounts(
                    Arrays.asList(keys().username(), keys().email()),
                    new SelectorCondition(keys().email(), Infix.EQUALS, args[0].toLowerCase())
            ).size();
            
            if (accountsWithEmail >= getConfig("config.yml").getInt("mail.accounts-per-email"))
            {
                sendMsg(player, _("accountsPerEmailLimitReached"));
                
                return true;
            }
            
            account.setEmail(args[0]);
            
            sendMsg(sender, _("changeEmail.success.self")
                    .replace("{0}", args[0].toLowerCase()));
            
            LogItCooldowns.activate(LogItCooldowns.CHANGEEMAIL, player);
        }
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
}
