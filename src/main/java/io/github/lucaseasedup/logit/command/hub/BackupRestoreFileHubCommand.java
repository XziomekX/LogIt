/*
 * BackupRestoreFileHubCommand.java
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
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.command.wizard.ConfirmationWizard;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BackupRestoreFileHubCommand extends HubCommand
{
    public BackupRestoreFileHubCommand()
    {
        super("backup restore file", new String[] {"filename"}, "logit.backup.restore", false, true,
                new CommandHelpLine.Builder()
                        .command("logit backup restore file")
                        .descriptionLabel("subCmdDesc.backup.restore.filename")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args)
    {
        final File selectedBackup = getBackupManager().getBackupFile(args[0]);
        
        if (selectedBackup == null)
        {
            sendMsg(sender, _("restoreBackup.backupNotFound")
                    .replace("{0}", args[0]));
            
            return;
        }
        
        Date selectedBackupDate;
        
        try
        {
            selectedBackupDate = getBackupManager().parseBackupFilename(selectedBackup.getName());
        }
        catch (ParseException ex)
        {
            sendMsg(sender, _("restoreBackup.fail")
                    .replace("{0}", selectedBackup.getName()));
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, _("restoreBackup.confirm.header"));
        sendMsg(sender, _("restoreBackup.confirm.typeToProceed"));
        sendMsg(sender, "");
        sendMsg(sender, _("restoreBackup.confirm.filename")
                .replace("{0}", selectedBackup.getName()));
        sendMsg(sender, _("restoreBackup.confirm.date")
                .replace("{0}", selectedBackupDate.toString()));
        sendMsg(sender, "");
        sendMsg(sender, _("restoreBackup.confirm.typeToCancel"));
        
        new ConfirmationWizard(sender, "proceed", new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getBackupManager().restoreBackup(args[0]);
                    
                    if (sender instanceof Player)
                    {
                        sendMsg(sender, _("restoreBackup.success")
                                .replace("{0}", args[0]));
                    }
                }
                catch (FileNotFoundException | ReportedException ex)
                {
                    if (sender instanceof Player)
                    {
                        sendMsg(sender, _("restoreBackup.fail")
                                .replace("{0}", args[0]));
                    }
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }).createWizard();
    }
}
