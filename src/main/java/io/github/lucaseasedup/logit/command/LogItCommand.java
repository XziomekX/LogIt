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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class LogItCommand extends LogItCoreObject implements TabExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        HubCommand hubCommand = findHubCommand(args);
        
        if (hubCommand == null)
        {
            if (sender instanceof Player && !sender.hasPermission("logit"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else
            {
                sendMsg(sender, _("typeForHelp"));
            }
            
            return true;
        }
        
        if (sender instanceof Player)
        {
            if (!sender.hasPermission(hubCommand.getPermission()))
            {
                sendMsg(sender, _("noPerms"));
                
                return true;
            }
        }
        else if (hubCommand.isPlayerOnly())
        {
            sendMsg(sender, _("onlyForPlayers"));
            
            return true;
        }
        
        if (hubCommand.isRunningCoreRequired() && !isCoreStarted())
        {
            sendMsg(sender, _("coreNotStarted"));
            
            return true;
        }
        
        String[] subcommandWords = hubCommand.getSubcommand().split("\\s+");
        
        if (args.length < subcommandWords.length + hubCommand.getParams().size())
        {
            StringBuilder usageParams = new StringBuilder();
            List<String> params = hubCommand.getParams();
            
            for (int i = 0; i < params.size(); i++)
            {
                if (args.length - subcommandWords.length > i)
                {
                    usageParams.append(_("cmdUsage.param")
                            .replace("{0}", params.get(i)));
                }
                else
                {
                    usageParams.append(_("cmdUsage.missingParam")
                            .replace("{0}", params.get(i)));
                }
            }
            
            CommandHelpLine helpLine = hubCommand.getHelpLine();
            
            if (helpLine.hasOptionalParam())
            {
                usageParams.append(_("cmdUsage.optionalParam")
                        .replace("{0}", helpLine.getOptionalParam()));
            }
            
            sendMsg(sender, _("cmdUsage")
                    .replace("{0}", helpLine.getCommand())
                    .replace("{1}", usageParams.toString()));
            
            return true;
        }
        
        String[] hubCommandArgs = translateHubCommandArgs(hubCommand, args);
        
        hubCommand.execute(sender, hubCommandArgs);
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command cmd,
                                      String label,
                                      String[] args)
    {
        HubCommand hubCommand = findHubCommand(args);
        
        if (hubCommand != null)
        {
            String[] subcommandWords = hubCommand.getSubcommand().split("\\s+");
            String[] hubCommandArgs;
            
            if (subcommandWords.length <= args.length - 1)
            {
                hubCommandArgs = Arrays.copyOfRange(args, subcommandWords.length, args.length);
            }
            else
            {
                hubCommandArgs = new String[0];
            }
            
            return hubCommand.complete(sender, hubCommandArgs);
        }
        
        return null;
    }
    
    private HubCommand findHubCommand(String[] args)
    {
        hubCommandSearch:
        for (Iterator<HubCommand> it = HubCommands.iterator(); it.hasNext();)
        {
            HubCommand hubCommand = it.next();
            String[] subcommandWords = hubCommand.getSubcommand().split("\\s+");
            
            if (subcommandWords.length > args.length)
                continue;
            
            for (int i = 0; i < subcommandWords.length; i++)
            {
                if (!subcommandWords[i].equalsIgnoreCase(args[i]))
                {
                    continue hubCommandSearch;
                }
            }
            
            return hubCommand;
        }
        
        return null;
    }
    
    /**
     * This method requires that there are enough elements in {@code args},
     * otherwise it will throw {@code IndexOutOfBoundsException}.
     */
    private String[] translateHubCommandArgs(HubCommand hubCommand, String[] args)
    {
        List<String> params = hubCommand.getParams();
        String[] subcommandWords = hubCommand.getSubcommand().split("\\s+");
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
        
        return hubCommandArgs;
    }
}
