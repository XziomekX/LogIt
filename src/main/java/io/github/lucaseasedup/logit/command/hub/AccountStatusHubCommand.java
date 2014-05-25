/*
 * AccountStatusHubCommand.java
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
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import org.bukkit.command.CommandSender;

public final class AccountStatusHubCommand extends HubCommand
{
    public AccountStatusHubCommand()
    {
        super("account status", new String[] {"username"}, "logit.account.status", false, true,
                new CommandHelpLine.Builder()
                        .command("logit account status")
                        .descriptionLabel("subCmdDesc.account.status")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        StringBuilder status = new StringBuilder(); 
        
        if (getAccountManager().isRegistered(args[0]))
        {
            status.append(_("accountStatus.registered"));
        }
        else
        {
            status.append(_("accountStatus.notRegistered"));
        }
        
        sendMsg(sender, _("accountStatus")
                .replace("{0}", args[0])
                .replace("{1}", status.toString()));
    }
}
