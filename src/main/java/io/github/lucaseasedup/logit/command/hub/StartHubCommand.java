package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.common.FatalReportedException;
import org.bukkit.command.CommandSender;

public final class StartHubCommand extends HubCommand
{
    public StartHubCommand()
    {
        super("start", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.start")
                        .playerOnly(false)
                        .runningCoreRequired(false)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit start")
                        .descriptionLabel("subCmdDesc.start")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (isCoreStarted())
        {
            sendMsg(sender, t("coreAlreadyStarted"));
            
            return;
        }
        
        try
        {
            if (!getCore().start().isCancelled())
            {
                sendMsg(sender, t("startCore.success"));
            }
            else
            {
                sendMsg(sender, t("startCore.fail"));
            }
        }
        catch (FatalReportedException ex)
        {
            sendMsg(sender, t("startCore.fail"));
        }
    }
}
