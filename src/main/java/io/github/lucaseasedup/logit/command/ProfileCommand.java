/*
 * ProfileCommand.java
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
package io.github.lucaseasedup.logit.command;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
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
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.profile.view.self"))
            {
                sendMsg(sender, _("noPerms"));
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
                sendMsg(sender, _("noPerms"));
            }
            else
            {
                if (!getCore().getProfileManager().containsProfile(args[1]))
                {
                    sendMsg(sender, _("profile.view.profileNotFound"));
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
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.profile.edit.self"))
            {
                sendMsg(sender, _("noPerms"));
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
                sendMsg(sender, _("noPerms"));
            }
            else
            {
                new ProfileEditWizard(sender, args[1]).createWizard();
            }
        }
        else
        {
            sendMsg(sender, _("incorrectParamCombination"));
        }
        
        return true;
    }
}
