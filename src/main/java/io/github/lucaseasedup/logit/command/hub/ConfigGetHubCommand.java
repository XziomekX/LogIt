/*
 * ConfigGetHubCommand.java
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

public final class ConfigGetHubCommand extends HubCommand
{
    public ConfigGetHubCommand()
    {
        super("config get", new String[] {"path"}, "logit.config.get", false, true,
                new CommandHelpLine.Builder()
                        .command("logit config get")
                        .descriptionLabel("subCmdDesc.config.get")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!getConfig().contains(args[2]))
        {
            sendMsg(sender, _("config.propertyNotFound")
                    .replace("{0}", args[0]));
            
            return;
        }
        
        sendMsg(sender, _("config.get.property")
                .replace("{0}", args[0])
                .replace("{1}", getConfig().toString(args[0])));
    }
}
