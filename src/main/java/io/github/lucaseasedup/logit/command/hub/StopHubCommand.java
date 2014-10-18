package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.command.wizard.ConfirmationWizard;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class StopHubCommand extends HubCommand
{
    public StopHubCommand()
    {
        super("stop", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.stop")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit stop")
                        .descriptionLabel("subCmdDesc.stop")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, String[] args)
    {
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("stopCore.confirm.areYouSure"));
        sendMsg(sender, t("stopCore.confirm.vulnerabilityInfo"));
        sendMsg(sender, t("stopCore.confirm.typeToConfirm"));
        
        new ConfirmationWizard(sender, "stop", new Runnable()
        {
            @Override
            public void run()
            {
                getCore().stop();
                
                sendMsg(sender, t("stopCore.success"));
            }
        }).createWizard();
    }
}
