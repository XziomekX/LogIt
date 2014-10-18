package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.config.TimeString;
import io.github.lucaseasedup.logit.config.TimeUnit;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import org.bukkit.command.CommandSender;

public final class BackupRestoreTimeHubCommand extends HubCommand
{
    public BackupRestoreTimeHubCommand()
    {
        super("backup restore time", new String[] {"time"},
                new CommandAccess.Builder()
                        .permission("logit.backup.restore")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit backup restore time")
                        .descriptionLabel("subCmdDesc.backup.restore.time")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        File[] backups = getBackupManager().getBackups();
        
        long currentTimeMillis = System.currentTimeMillis();
        long desiredDeltaTimeMillis = TimeString.decode(args[0], TimeUnit.MILLISECONDS);
        long smallestDeltaDifference = Long.MAX_VALUE;
        
        File closestBackup = null;
        
        for (File backup : backups)
        {
            try
            {
                Date backupDate = getBackupManager().parseBackupFilename(backup.getName());
                long deltaTimeMillis = (currentTimeMillis - backupDate.getTime());
                long deltaDifference = Math.abs(desiredDeltaTimeMillis - deltaTimeMillis);
                
                if (closestBackup == null || deltaDifference < smallestDeltaDifference)
                {
                    closestBackup = backup;
                    smallestDeltaDifference = deltaDifference;
                }
            }
            catch (ParseException ex)
            {
                // If a ParseException has been thrown, the file is probably not a backup,
                // so we skip it without notice.
            }
        }
        
        if (closestBackup == null)
        {
            sendMsg(sender, t("restoreBackup.noBackups"));
            
            return;
        }
        
        new BackupRestoreFileHubCommand().execute(sender, new String[] {closestBackup.getName()});
    }
}
