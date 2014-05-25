/*
 * ReloadHubCommand.java
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
import io.github.lucaseasedup.logit.FatalReportedException;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ReloadHubCommand extends HubCommand
{
    public ReloadHubCommand()
    {
        super("reload", new String[] {}, "logit.reload", false, true,
                new CommandHelpLine.Builder()
                        .command("logit reload")
                        .descriptionLabel("subCmdDesc.reload")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, String[] args)
    {
        try
        {
            getCore().restart();
            
            if (sender instanceof Player)
            {
                sendMsg(sender, _("reloadPlugin.success"));
            }
        }
        catch (FatalReportedException ex)
        {
            if (sender instanceof Player)
            {
                sendMsg(sender, _("reloadPlugin.fail"));
            }
        }
    }
}
