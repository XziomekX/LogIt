/*
 * GlobalpassSetHubCommand.java
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
import org.bukkit.entity.Player;

public final class GlobalpassSetHubCommand extends HubCommand
{
    public GlobalpassSetHubCommand()
    {
        super("globalpass set", new String[] {"password"}, "logit.globalpass.set", false, true,
                new CommandHelpLine.Builder()
                        .command("logit globalpass set")
                        .descriptionLabel("subCmdDesc.globalpass.set")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, String[] args)
    {
        int minPasswordLength = getConfig().getInt("password.min-length");
        int maxPasswordLength = getConfig().getInt("password.max-length");
        
        if (args[0].length() < minPasswordLength)
        {
            sendMsg(sender, _("passwordTooShort")
                    .replace("{0}", String.valueOf(minPasswordLength)));
            
            return;
        }
        
        if (args[0].length() > maxPasswordLength)
        {
            sendMsg(sender, _("passwordTooLong")
                    .replace("{0}", String.valueOf(maxPasswordLength)));
            
            return;
        }
        
        getCore().changeGlobalPassword(args[0]);
        
        if (sender instanceof Player)
        {
            sendMsg(sender, _("globalpass.set.success"));
        }
    }
}
