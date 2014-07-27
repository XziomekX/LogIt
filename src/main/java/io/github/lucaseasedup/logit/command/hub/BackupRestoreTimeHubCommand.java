/*
 * BackupRestoreTimeHubCommand.java
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
package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.TimeString;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.io.comparator.NameFileComparator;
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
        File[] backups = getBackupManager().getBackups(NameFileComparator.NAME_COMPARATOR);
        
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
            sendMsg(sender, _("restoreBackup.noBackups"));
            
            return;
        }
        
        new BackupRestoreFileHubCommand().execute(sender, new String[] {closestBackup.getName()});
    }
}
