/*
 * IpcountHubCommand.java
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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class IpcountHubCommand extends HubCommand
{
    public IpcountHubCommand()
    {
        super("ipcount", new String[] {"ip"},
                new CommandAccess.Builder()
                        .permission("logit.ipcount")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit ipcount")
                        .descriptionLabel("subCmdDesc.ipcount")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        List<Account> accounts = getAccountManager().selectAccounts(
                Arrays.asList(
                        keys().username(),
                        keys().ip()
                ),
                new SelectorCondition(keys().ip(), Infix.EQUALS, args[0])
        );
        
        sendMsg(sender, _("ipcount")
                .replace("{0}", args[0])
                .replace("{1}", (accounts != null) ? String.valueOf(accounts.size()) : "?"));
    }
}
