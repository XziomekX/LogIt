/*
 * ProfileCommand.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.profile.field.Field;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ProfileCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        if (args.length == 1 && args[0].equalsIgnoreCase("view"))
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.profile.view.self"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                List<Field> fields = getCore().getProfileManager().getDefinedFields();
                
                sender.sendMessage("");
                sender.sendMessage(getMessage("PROFILE_HEADER")
                        .replace("%player%", p.getName()));
                sender.sendMessage(getMessage("ORANGE_HORIZONTAL_LINE"));
                
                if (!fields.isEmpty())
                {
                    for (Field field : fields)
                    {
                        Object value = getCore().getProfileManager()
                                .getProfileObject(p.getName(), field.getName());
                        
                        if (value == null)
                        {
                            value = "";
                        }
                        
                        sender.sendMessage(getMessage("PROFILE_FIELD")
                                .replace("%field%", field.getName())
                                .replace("%value%", value.toString()));
                    }
                }
                else
                {
                    sender.sendMessage(getMessage("PROFILE_NO_FIELDS"));
                }
            }
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("view"))
        {
            if (p != null && !p.hasPermission("logit.profile.view.others"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                
            }
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("edit"))
        {
            if (p != null && !p.hasPermission("logit.profile.edit.self"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                
            }
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("edit"))
        {
            if (p != null && !p.hasPermission("logit.profile.edit.others"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                
            }
        }
        else
        {
            sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
}
