/*
 * BackupForceHubCommand.java
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
