package io.github.lucaseasedup.logit.command.hub;

import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.command.wizard.ConvertWizard;
import org.bukkit.command.CommandSender;

public final class ConvertHubCommand extends HubCommand
{
    public ConvertHubCommand()
    {
        super("convert", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.convert")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit convert")
                        .descriptionLabel("subCmdDesc.convert")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        new ConvertWizard(sender).createWizard();
    }
}
