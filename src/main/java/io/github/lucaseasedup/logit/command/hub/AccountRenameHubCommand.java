/*
 * AccountRenameHubCommand.java
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
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.command.wizard.ConfirmationWizard;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class AccountRenameHubCommand extends HubCommand
{
    public AccountRenameHubCommand()
    {
        super("account rename", new String[] {"username", "newUsername"},
                "logit.account.rename", false, true,
                new CommandHelpLine.Builder()
                        .command("logit account rename")
                        .descriptionLabel("subCmdDesc.account.rename")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args)
    {
        if (!getAccountManager().isRegistered(args[0]))
        {
            sendMsg(sender, _("notRegistered.others")
                    .replace("{0}", args[0].toLowerCase()));
            
            return;
        }
        
        if (!args[1].matches(getConfig("secret.yml").getString("username.regex")))
        {
            sendMsg(sender, _("accountRename.usernameInvalid"));
            
            return;
        }
        
        int minUsernameLength = getConfig("secret.yml").getInt("username.min-length");
        int maxUsernameLength = getConfig("secret.yml").getInt("username.max-length");
        
        if (args[1].length() < minUsernameLength)
        {
            sendMsg(sender, _("accountRename.usernameTooShort")
                    .replace("{0}", String.valueOf(minUsernameLength)));
            
            return;
        }
        
        if (args[1].length() > maxUsernameLength)
        {
            sendMsg(sender, _("accountRename.usernameTooLong")
                    .replace("{0}", String.valueOf(maxUsernameLength)));
            
            return;
        }
        
        if (CollectionUtils.containsIgnoreCase(args[1],
                getConfig("config.yml").getStringList("prohibited-usernames")))
        {
            sendMsg(sender, _("accountRename.usernameProhibited"));
            
            return;
        }
        
        if (isPlayerOnline(args[1]))
        {
            sendMsg(sender, _("accountRename.usernameTaken"));
            
            return;
        }
        else
        {
            if (getAccountManager().isRegistered(args[1]))
            {
                sendMsg(sender, _("accountRename.usernameTaken"));
                
                return;
            }
        }
        
        sendMsg(sender, _("accountRename.confirm")
                .replace("{0}", args[0].toLowerCase())
                .replace("{1}", args[1].toLowerCase()));
        
        new ConfirmationWizard(sender, "rename", new Runnable()
        {
            @Override
            public void run()
            {
                getAccountManager().renameAccount(args[0], args[1]);
                
                sendMsg(sender, _("accountRename.success")
                        .replace("{0}", args[1].toLowerCase()));
            }
        }).createWizard();
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
