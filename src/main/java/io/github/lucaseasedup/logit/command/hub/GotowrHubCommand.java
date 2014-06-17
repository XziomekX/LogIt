/*
 * GotowrHubCommand.java
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

import io.github.lucaseasedup.logit.command.CommandHelpLine;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GotowrHubCommand extends HubCommand
{
    public GotowrHubCommand()
    {
        super("gotowr", new String[] {}, "logit.gotowr", true, true,
                new CommandHelpLine.Builder()
                        .command("logit gotowr")
                        .descriptionLabel("subCmdDesc.gotowr")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Location waitingRoomLocation =
                getConfig("config.yml").getLocation("waiting-room.location").toBukkitLocation();
        
        ((Player) sender).teleport(waitingRoomLocation);
    }
}
