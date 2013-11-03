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
import io.github.lucaseasedup.logit.command.wizard.ProfileEditWizard;
import io.github.lucaseasedup.logit.command.wizard.ProfileViewWizard;
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
                new ProfileViewWizard(sender, p.getName()).createWizard();
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
                if (!getCore().getProfileManager().containsProfile(args[1]))
                {
                    sender.sendMessage(getMessage("PROFILE_VIEW_PROFILE_NOT_FOUND"));
                }
                else
                {
                    new ProfileViewWizard(sender, args[1]).createWizard();
                }
            }
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("edit"))
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.profile.edit.self"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                new ProfileEditWizard(sender, p.getName()).createWizard();
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
                new ProfileEditWizard(sender, args[1]).createWizard();
            }
        }
        else
        {
            sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
}
