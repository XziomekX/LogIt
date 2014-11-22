package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class VersionHubCommand extends HubCommand
{
    public VersionHubCommand()
    {
        super("version", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.version")
                        .playerOnly(false)
                        .runningCoreRequired(false)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit version")
                        .descriptionLabel("subCmdDesc.version")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("aboutPlugin.header"));
        sendMsg(sender, t("aboutPlugin.pluginVersion")
                .replace("{0}", getPlugin().getDescription().getVersion()));
        sendMsg(sender, t("aboutPlugin.javaVersion")
                .replace("{0}", System.getProperty("java.version")));
        sendMsg(sender, t("aboutPlugin.author"));
    }
}
