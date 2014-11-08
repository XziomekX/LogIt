package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
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
                
                helpLine = helpLine.replace("{0}", command + params);
                helpLine = helpLine.replace("{1}", description);
                
                sendMsg(sender, helpLine);
            }
        }
    }
}
