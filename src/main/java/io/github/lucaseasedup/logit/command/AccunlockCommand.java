/*
 * AccunlockCommand.java
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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AccunlockCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        if (args.length <= 1)
        {
            if (p != null && !p.hasPermission("logit.accunlock"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 1)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "username"));
            }
            else if (p != null && p.getName().equalsIgnoreCase(args[0]))
            {
                sender.sendMessage(getMessage("CANNOT_UNLOCK_YOURSELF"));
            }
            else if (!getAccountManager().isRegistered(args[0]))
            {
                sender.sendMessage(getMessage("NOT_REGISTERED_OTHERS").replace("%player%", args[0]));
            }
            else
            {
                try
                {
                    AccountKeys keys = getAccountManager().getKeys();
                    getAccountStorage().updateEntries(getAccountManager().getUnit(),
                            new Storage.Entry.Builder()
                                .put(keys.is_locked(), "0")
                                .build(),
                            new SelectorCondition(
                                keys.username(), Infix.EQUALS, args[0].toLowerCase()
                            ));
                    
                    String playerName = args[0]; 
                    
                    if (PlayerUtils.isPlayerOnline(args[0]))
                    {
                        playerName = PlayerUtils.getPlayerName(args[0]);
                        
                        PlayerUtils.getPlayer(args[0])
                                .sendMessage(getMessage("ACCUNLOCK_SUCCESS_SELF"));
                    }
                    
                    sender.sendMessage(getMessage("ACCUNLOCK_SUCCESS_OTHERS")
                            .replace("%player%", playerName));
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, ex);
                    
                    sender.sendMessage(getMessage("UNEXPECTED_ERROR"));
                }
            }
        }
        else
        {
            sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
        }
        
        return true;
    }
}
