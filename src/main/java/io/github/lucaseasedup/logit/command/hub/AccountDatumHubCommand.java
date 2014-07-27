/*
 * AccountDatumHubCommand.java
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
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AccountDatumHubCommand extends HubCommand
{
    public AccountDatumHubCommand()
    {
        super(
                "account datum",
                new String[] {"username", "key"},
                "logit.account.datum",
                false,
                true,
                new CommandHelpLine.Builder()
                        .command("logit account datum")
                        .descriptionLabel("subCmdDesc.account.datum")
                        .build()
        );
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        List<String> queryKeys = new ArrayList<>();
        
        if (!args[1].equals(keys().username()))
        {
            queryKeys.add(keys().username());
        }
        
        queryKeys.add(args[1]);
        
        Account account = getAccountManager().selectAccount(args[0], queryKeys);
        
        if (account == null)
        {
            sendMsg(sender, _("notRegistered.others")
                    .replace("{0}", args[0]));
            
            return;
        }
        
        if (!account.getEntry().containsKey(args[1]))
        {
            sendMsg(sender, _("accountDatum.keyNotFound")
                    .replace("{0}", args[1]));
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, _("accountDatum.result")
                .replace("{0}", args[0].toLowerCase())
                .replace("{1}", args[1])
                .replace("{2}", account.getEntry().get(args[1])));
    }
    
    @Override
    public List<String> complete(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tab-completion"))
            return null;
        
        if (args.length == 1)
        {
            return getTabCompleter().completeUsername(args[0]);
        }
        
        return null;
    }
}
