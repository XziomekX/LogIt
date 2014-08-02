/*
 * HelpHubCommand.java
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

import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.util.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.util.Iterator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HelpHubCommand extends HubCommand
{
    public HelpHubCommand()
    {
        super("help", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.help")
                        .playerOnly(false)
                        .runningCoreRequired(false)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit help")
                        .descriptionLabel("subCmdDesc.help")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        for (Iterator<HubCommand> it = HubCommands.iterator(); it.hasNext();)
        {
            HubCommand hubCommand = it.next();
            
            if ((!(sender instanceof Player) || sender.hasPermission(hubCommand.getPermission()))
                    && !(hubCommand.isPlayerOnly() && !(sender instanceof Player))
                    && (getCore().isStarted() || !hubCommand.isRunningCoreRequired()))
            {
                StringBuilder params = new StringBuilder();
                
                for (String param : hubCommand.getParams())
                {
                    params.append(" <");
                    params.append(param);
                    params.append(">");
                }
                
                if (hubCommand.getHelpLine().hasOptionalParam())
                {
                    params.append(" [");
                    params.append(hubCommand.getHelpLine().getOptionalParam());
                    params.append("]");
                }
                
                String helpLine = t("subCmdHelpLine");
                String command = hubCommand.getHelpLine().getCommand();
                String description = t(hubCommand.getHelpLine().getDescriptionLabel());
                
                helpLine = helpLine.replace("{0}", command + params.toString());
                helpLine = helpLine.replace("{1}", description);
                
                sendMsg(sender, helpLine);
            }
        }
    }
}
