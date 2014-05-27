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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BackupRemoveHubCommand extends HubCommand
{
    public BackupRemoveHubCommand()
    {
        super("backup remove", new String[] {"amount"}, "logit.backup.remove", false, true,
                new CommandHelpLine.Builder()
                        .command("logit backup remove")
                        .descriptionLabel("subCmdDesc.backup.remove")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, String[] args)
    {
        try
        {
            int amount = Integer.parseInt(args[0]);
            
            if (amount > getConfig().getInt("backup.manual-remove-limit"))
            {
                amount = getConfig().getInt("backup.manual-remove-limit");
            }
            
            int effectiveAmount = getBackupManager().removeBackups(amount);
            
            if (sender instanceof Player)
            {
                sendMsg(sender, _("removeBackups.success")
                        .replace("{0}", String.valueOf(effectiveAmount)));
            }
        }
        catch (NumberFormatException ex)
        {
            sendMsg(sender, _("invalidParam")
                    .replace("{0}", "amount"));
        }
    }
}
