/*
 * BackupRestoreWizard.java
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
package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BackupRestoreWizard extends Wizard
{
    /**
     * @param sender   the {@code CommandSender}.
     * @param backup   the backup {@code File}.
     * @param callback the backup restoration process callback.
     * 
     * @throws ParseException           if the backup name could not be parsed as a date.
     * @throws IllegalArgumentException if {@code sender}, {@code backup}
     *                                  or {@code callback} is {@code null}.
     */
    public BackupRestoreWizard(CommandSender sender,
                               File backup,
                               Runnable callback) throws ParseException
    {
        super(sender, null);
        
        if (sender == null || backup == null || callback == null)
            throw new IllegalArgumentException();
        
        this.backup = backup;
        this.backupDate = getBackupManager().parseBackupFilename(backup.getName());
        this.callback = callback;
    }
    
    @Override
    protected void onCreate()
    {
        if (getSender() instanceof Player)
        {
            sendMessage("");
        }
        
        sendMessage(_("restoreBackup.confirm.header"));
        sendMessage(_("restoreBackup.confirm.typeToProceed"));
        sendMessage("");
        sendMessage(_("restoreBackup.confirm.filename")
                .replace("{0}", backup.getName()));
        sendMessage(_("restoreBackup.confirm.date")
                .replace("{0}", backupDate.toString()));
        sendMessage("");
        sendMessage(_("restoreBackup.confirm.typeToCancel"));
    }
    
    @Override
    protected void onMessage(String message)
    {
        if (message.equalsIgnoreCase("proceed"))
        {
            callback.run();
        }
        else
        {
            sendMessage(_("wizardCancelled"));
        }
        
        cancelWizard();
    }
    
    private File backup;
    private Date backupDate;
    private Runnable callback;
}
