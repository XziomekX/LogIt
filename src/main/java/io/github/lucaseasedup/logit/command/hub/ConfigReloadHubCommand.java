/*
 * ConfigReloadHubCommand.java
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
import io.github.lucaseasedup.logit.config.InvalidPropertyValueException;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

public final class ConfigReloadHubCommand extends HubCommand
{
    public ConfigReloadHubCommand()
    {
        super("config reload", new String[] {},
                new CommandAccess.Builder()
                        .permission("logit.config.reload")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit config reload")
                        .descriptionLabel("subCmdDesc.config.reload")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        try
        {
            getConfigurationManager().loadAll();
            
            log(Level.INFO, t("reloadConfig.success"));
            
            if (sender instanceof Player)
            {
                sendMsg(sender, t("reloadConfig.success"));
            }
        }
        catch (IOException | InvalidConfigurationException | InvalidPropertyValueException ex)
        {
            ex.printStackTrace();
            
            sendMsg(sender, t("reloadConfig.fail"));
        }
    }
}
