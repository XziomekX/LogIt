/*
 * LogItCommand.java
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
import io.github.lucaseasedup.logit.command.hub.HubCommand;
import io.github.lucaseasedup.logit.command.hub.HubCommands;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LogItCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        boolean hubCommandFound = false;
        boolean hubCommandExecuted = false;
        
        hubCommandSearch:
        for (Iterator<HubCommand> it = HubCommands.iterator(); it.hasNext();)
        {
            HubCommand hubCommand = it.next();
            
            String[] subcommandWords = hubCommand.getSubcommand().split("\\s+");
            List<String> params = hubCommand.getParams();
            
            if (subcommandWords.length > args.length)
                continue;
            
            for (int i = 0; i < subcommandWords.length; i++)
            {
                if (!subcommandWords[i].equalsIgnoreCase(args[i]))
                {
                    continue hubCommandSearch;
                }
            }
            
            hubCommandFound = true;
            
            for (int i = params.size() - 1; i >= 0; i--)
            {
                if (args.length < subcommandWords.length + i + 1)
                {
                    sendMsg(sender, _("paramMissing")
                            .replace("{0}", params.get(i)));
                    
                    break hubCommandSearch;
                }
            }
            
            if (sender instanceof Player && !sender.hasPermission(hubCommand.getPermission()))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (hubCommand.isPlayerOnly() && !(sender instanceof Player))
            {
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (hubCommand.requiresRunningCore() && !isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                String[] hubCommandArgs = new String[params.size()];
                
                System.arraycopy(args, subcommandWords.length, hubCommandArgs, 0, params.size());
                
                for (int i = subcommandWords.length + params.size(); i < args.length; i++)
                {
                    if (hubCommandArgs.length == 0)
                    {
                        hubCommandArgs = new String[1];
                    }
                    
                    if (hubCommandArgs[hubCommandArgs.length - 1] == null)
                    {
                        hubCommandArgs[hubCommandArgs.length - 1] = "";
                    }
                    
                    if (params.size() != 0 || i == subcommandWords.length + params.size() + 1)
                    {
                        hubCommandArgs[hubCommandArgs.length - 1] += " ";
                    }
                    
                    hubCommandArgs[hubCommandArgs.length - 1] += args[i];
                }
                
                hubCommand.execute(sender, hubCommandArgs);
                hubCommandExecuted = true;
            }
            
            break;
        }
        
        if (!hubCommandFound)
        {
            if (sender instanceof Player && !sender.hasPermission("logit"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else
            {
                sendMsg(sender, _("typeForHelp"));
            }
        }
        
        return true;
    }
}
