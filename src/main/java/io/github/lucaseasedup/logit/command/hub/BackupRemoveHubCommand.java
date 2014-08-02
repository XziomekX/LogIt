/*
 * BackupRemoveHubCommand.java
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

import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.util.MessageHelper.t;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.command.wizard.ConfirmationWizard;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BackupRemoveHubCommand extends HubCommand
{
    public BackupRemoveHubCommand()
    {
        super("backup remove", new String[] {"amount"},
                new CommandAccess.Builder()
                        .permission("logit.backup.remove")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit backup remove")
                        .descriptionLabel("subCmdDesc.backup.remove")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, final String[] args)
    {
        final int amount;
        
        try
        {
            amount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ex)
        {
            sendMsg(sender, t("invalidParam")
                    .replace("{0}", "amount"));
            
            return;
        }
        
        if (amount < 1)
        {
            sendMsg(sender, t("removeBackups.noBackupsSelected"));
            
            return;
        }
        
        int manualRemoveLimit = getConfig("config.yml").getInt("backup.manualRemoveLimit");
        File[] backups = getBackupManager().getBackups();
        
        if (backups.length == 0)
        {
            sendMsg(sender, t("removeBackups.noBackupsAvailable"));
            
            return;
        }
        
        final int limitedAmount;
        
        if (amount > manualRemoveLimit)
        {
            limitedAmount = manualRemoveLimit;
            
            sendMsg(sender, t("removeBackups.confirm.limitedAmount")
                    .replace("{0}", String.valueOf(limitedAmount)));
        }
        else
        {
            limitedAmount = amount;
            
            sendMsg(sender, t("removeBackups.confirm.amount")
                    .replace("{0}", String.valueOf(limitedAmount)));
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, t("removeBackups.confirm.header"));
        
        if (limitedAmount == 1)
        {
            sendMsg(sender, t("removeBackups.confirm.typeToRemove.single"));
        }
        else
        {
            sendMsg(sender, t("removeBackups.confirm.typeToRemove.range"));
        }
        
        sendMsg(sender, "");
        
        if (limitedAmount > 1)
        {
            if (limitedAmount < amount)
            {
                sendMsg(sender, t("removeBackups.confirm.limitedAmount")
                        .replace("{0}", String.valueOf(limitedAmount)));
            }
            else
            {
                sendMsg(sender, t("removeBackups.confirm.amount")
                        .replace("{0}", String.valueOf(limitedAmount)));
            }
        }
        
        File firstBackup = backups[0];
        Date firstBackupDate = null;
        
        try
        {
            firstBackupDate = getBackupManager().parseBackupFilename(firstBackup.getName());
        }
        catch (ParseException ex)
        {
        }
        
        if (limitedAmount == 1)
        {
            sendMsg(sender, t("removeBackups.confirm.date.single")
                    .replace("{0}",
                            (firstBackupDate != null) ? firstBackupDate.toString() : "?"));
        }
        else
        {
            File lastBackup = backups[Math.min(limitedAmount, backups.length) - 1];
            Date lastBackupDate = null;
            
            try
            {
                lastBackupDate = getBackupManager().parseBackupFilename(lastBackup.getName());
            }
            catch (ParseException ex)
            {
            }
            
            sendMsg(sender, t("removeBackups.confirm.date.range.start")
                    .replace("{0}",
                            (firstBackupDate != null) ? firstBackupDate.toString() : "?"));
            sendMsg(sender, t("removeBackups.confirm.date.range.end")
                    .replace("{0}",
                            (lastBackupDate != null) ? lastBackupDate.toString() : "?"));
        }
        
        sendMsg(sender, "");
        sendMsg(sender, t("removeBackups.confirm.typeToCancel"));
        
        new ConfirmationWizard(sender, "remove", new Runnable()
        {
            @Override
            public void run()
            {
                int effectiveAmount = getBackupManager().removeBackups(limitedAmount);
                
                if (sender instanceof Player)
                {
                    sendMsg(sender, t("removeBackups.success")
                            .replace("{0}", String.valueOf(effectiveAmount)));
                }
            }
        }).createWizard();
    }
}
