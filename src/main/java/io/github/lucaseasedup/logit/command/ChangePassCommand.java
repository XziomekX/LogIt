/*
 * ChangePassCommand.java
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
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class ChangePassCommand extends LogItCoreObject implements TabExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }
        
        int minPasswordLength = getConfig("config.yml").getInt("passwords.minLength");
        int maxPasswordLength = getConfig("config.yml").getInt("passwords.maxLength");
        
        if (args.length > 0 && args[0].equals("-x") && args.length <= 3)
        {
            if (player != null && !player.hasPermission("logit.changepass.others"))
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
                        .replace("{0}", "newpassword"));
                
                return true;
            }
            
            if (args[2].length() < minPasswordLength)
            {
                sendMsg(sender, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
                
                return true;
            }
            
            if (args[2].length() > maxPasswordLength)
            {
                sendMsg(sender, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
                
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
            
            account.changePassword(args[2]);
            
            sendMsg(args[1], _("changePassword.success.self"));
            sendMsg(sender, _("changePassword.success.others")
                    .replace("{0}", PlayerUtils.getPlayerRealName(args[1])));
            
            getConfig("stats.yml").set("passwordChanges",
                    getConfig("stats.yml").getInt("passwordChanges") + 1);
        }
        else if (args.length <= 3)
        {
            if (player == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
                
                return true;
            }
            
            if (!player.hasPermission("logit.changepass.self"))
            {
                sendMsg(player, _("noPerms"));
                
                return true;
            }
            
            if (args.length < 1)
            {
                sendMsg(player, _("paramMissing")
                        .replace("{0}", "oldpassword"));
                
                return true;
            }
            
            if (args.length < 2)
            {
                sendMsg(player, _("paramMissing")
                        .replace("{0}", "newpassword"));
                
                return true;
            }
            
            if (args.length < 3)
            {
                sendMsg(player, _("paramMissing")
                        .replace("{0}", "confirmpassword"));
                
                return true;
            }
            
            if (getCooldownManager().isCooldownActive(player, LogItCooldowns.CHANGEPASS))
            {
                getMessageDispatcher().sendCooldownMessage(player.getName(),
                        getCooldownManager().getCooldownMillis(player, LogItCooldowns.CHANGEPASS));
                
                return true;
            }
            
            if (args[1].length() < minPasswordLength)
            {
                sendMsg(player, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
                
                return true;
            }
            
            if (args[1].length() > maxPasswordLength)
            {
                sendMsg(player, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
                
                return true;
            }
            
            if (!args[1].equals(args[2]))
            {
                sendMsg(player, _("passwordsDoNotMatch"));
                
                return true;
            }
            
            Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                    keys().username(),
                    keys().salt(),
                    keys().password(),
                    keys().hashing_algorithm()
            ));
            
            if (account == null)
            {
                sendMsg(player, _("notRegistered.self"));
                
                return true;
            }
            
            if (!account.checkPassword(args[0]))
            {
                sendMsg(player, _("incorrectPassword"));
                
                return true;
            }
            
            account.changePassword(args[1]);
            
            sendMsg(sender, _("changePassword.success.self"));
            
            LogItCooldowns.activate(player, LogItCooldowns.CHANGEPASS);
            
            getConfig("stats.yml").set("passwordChanges",
                    getConfig("stats.yml").getInt("passwordChanges") + 1);
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
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 2 && args[0].equals("-x"))
        {
            if (sender instanceof Player
                    && !sender.hasPermission("logit.changepass.others"))
                return null;
            
            return getTabCompleter().completeUsername(args[1]);
        }
        
        return null;
    }
}
