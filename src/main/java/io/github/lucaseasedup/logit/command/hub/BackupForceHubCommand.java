package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.io.File;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BackupForceHubCommand extends HubCommand
{
    public BackupForceHubCommand()
    {
        super("backup force", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.backup.force")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit backup force")
                        .descriptionLabel("subCmdDesc.backup.force")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        File backupFile = getBackupManager().createBackupAsynchronously();
        
        if (sender instanceof Player)
        {
            sendMsg(sender, t("createBackup.willBeCreated")
                    .replace("{0}", backupFile.getName()));
        }
    }
}
