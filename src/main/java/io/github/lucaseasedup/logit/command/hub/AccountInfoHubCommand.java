/*
 * AccountInfoHubCommand.java
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
package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AccountInfoHubCommand extends HubCommand
{
    public AccountInfoHubCommand()
    {
        super("account info", new String[] {"username"},
                new CommandAccess.Builder()
                        .permission("logit.account.info")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit account info")
                        .descriptionLabel("subCmdDesc.account.info")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Account account = getAccountManager().selectAccount(args[0], Arrays.asList(
                keys().username(),
                keys().ip(),
                keys().email(),
                keys().last_active_date(),
                keys().reg_date(),
                keys().is_locked(),
                keys().display_name()
        ));
        
        if (account == null)
        {
            sendMsg(sender, t("notRegistered.others")
                    .replace("{0}", PlayerUtils.getPlayerRealName(args[0])));
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("accountInfo.header"));
        sendMsg(sender, t("accountInfo.username")
                .replace("{0}", account.getUsername()));
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.ip"))
        {
            sendMsg(sender, t("accountInfo.ip")
                    .replace("{0}", account.getIp()));
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.email"))
        {
            sendMsg(sender, t("accountInfo.email")
                    .replace("{0}", account.getEmail()));
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.lastactivedate"))
        {
            long lastActiveDate = account.getLastActiveDate();
            
            if (lastActiveDate < 0)
            {
                sendMsg(sender, t("accountInfo.lastActiveDate.never"));
            }
            else
            {
                sendMsg(sender, t("accountInfo.lastActiveDate")
                        .replace("{0}", new Date(account.getLastActiveDate() * 1000L).toString()));
            }
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.regdate"))
        {
            long registrationDate = account.getRegistrationDate();
            
            if (registrationDate < 0)
            {
                sendMsg(sender, t("accountInfo.registrationDate.never"));
            }
            else
            {
                sendMsg(sender, t("accountInfo.registrationDate")
                        .replace("{0}", new Date(account.getRegistrationDate() * 1000L).toString()));
            }
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.displayname"))
        {
            sendMsg(sender, t("accountInfo.displayName")
                    .replace("{0}", account.getDisplayName()));
        }
        
        if (!(sender instanceof Player)
                || sender.hasPermission("logit.account.info.islocked"))
        {
            if (account.isLocked())
            {
                sendMsg(sender, "");
                sendMsg(sender, t("accountInfo.locked")
                        .replace("{0}", account.getUsername()));
            }
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
    }
    
    @Override
    public List<String> complete(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 1)
        {
            return getTabCompleter().completeUsername(args[0]);
        }
        
        return null;
    }
}
