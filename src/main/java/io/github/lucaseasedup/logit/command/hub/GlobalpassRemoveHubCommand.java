/*
 * GlobalpassRemoveHubCommand.java
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

public final class GlobalpassRemoveHubCommand extends HubCommand
{
    public GlobalpassRemoveHubCommand()
    {
        super("globalpass remove", new String[] {}, "logit.globalpass.remove", false, true,
                new CommandHelpLine.Builder()
                        .command("logit globalpass remove")
                        .descriptionLabel("subCmdDesc.globalpass.remove")
                        .build());
    }
    
    @Override
    public void execute(final CommandSender sender, String[] args)
    {
        getCore().removeGlobalPassword();
        
        if (sender instanceof Player)
        {
            sendMsg(sender, _("globalpass.remove.success"));
        }
    }
}
