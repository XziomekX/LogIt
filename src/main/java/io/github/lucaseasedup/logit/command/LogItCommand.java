package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.command.hub.HubCommand;
import io.github.lucaseasedup.logit.command.hub.HubCommands;
import io.github.lucaseasedup.logit.util.ArrayUtils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class LogItCommand extends LogItCoreObject implements TabExecutor
{
    @Override
    public boolean onCommand(
            CommandSender sender, Command cmd, String label, String[] args
    )
    {
        HubCommand hubCommand = findHubCommand(args);
        
        if (hubCommand == null)
        {
            if (sender instanceof Player && !sender.hasPermission("logit"))
            {
                sendMsg(sender, t("noPerms"));
            }
            else
            {
                sendMsg(sender, t("typeForHelp"));
            }
            
            return true;
        }
        
        if (sender instanceof Player)
        {
            if (!sender.hasPermission(hubCommand.getPermission()))
            {
                sendMsg(sender, t("noPerms"));
                
                return true;
            }
        }
        else if (hubCommand.isPlayerOnly())
        {
            sendMsg(sender, t("onlyForPlayers"));
            
            return true;
        }
        
        if (hubCommand.isRunningCoreRequired() && !isCoreStarted())
        {
            sendMsg(sender, t("coreNotStarted"));
            
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
                    usageParams.append(t("cmdUsage.param")
                            .replace("{0}", params.get(i)));
                }
                else
                {
                    usageParams.append(t("cmdUsage.missingParam")
                            .replace("{0}", params.get(i)));
                }
            }
            
            CommandHelpLine helpLine = hubCommand.getHelpLine();
            
            if (helpLine.hasOptionalParam())
            {
                usageParams.append(t("cmdUsage.optionalParam")
                        .replace("{0}", helpLine.getOptionalParam()));
            }
            
            sendMsg(sender, t("cmdUsage")
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
        
        // If command found, complete its arguments.
        if (hubCommand != null)
        {
            String[] subcommandWords = hubCommand.getSubcommand().split("\\s+");
            String[] hubCommandArgs;
            
            if (subcommandWords.length <= args.length - 1)
            {
                hubCommandArgs = Arrays.copyOfRange(
                        args, subcommandWords.length, args.length
                );
            }
            else
            {
                hubCommandArgs = ArrayUtils.NO_STRINGS;
            }
            
            return hubCommand.complete(sender, hubCommandArgs);
        }
        // If command not found, complete command.
        else
        {
            return getTabCompleter().completeHubCommand(
                    StringUtils.join(args, " ")
            );
        }
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
            
            if (!params.isEmpty()
                    || i == subcommandWords.length + params.size() + 1)
            {
                hubCommandArgs[hubCommandArgs.length - 1] += " ";
            }
            
            hubCommandArgs[hubCommandArgs.length - 1] += args[i];
        }
        
        return hubCommandArgs;
    }
}
