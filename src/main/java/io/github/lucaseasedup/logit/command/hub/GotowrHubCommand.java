package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GotowrHubCommand extends HubCommand
{
    public GotowrHubCommand()
    {
        super("gotowr", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.gotowr")
                        .playerOnly(true)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit gotowr")
                        .descriptionLabel("subCmdDesc.gotowr")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Location location = getCore().getWaitingRoomLocation();
        
        if (location == null || location.getY() == 0)
        {
            sender.sendMessage(t("waitingRoomNotSet"));
        }
        else
        {
            ((Player) sender).teleport(location);
        }
    }
}
