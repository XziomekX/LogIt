/*
 * RememberCommand.java
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
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RememberCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        if (args.length == 0)
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.remember"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!getAccountManager().isRegistered(p.getName()))
            {
                sender.sendMessage(getMessage("CREATE_ACCOUNT_NOT_SELF"));
            }
            else if (!getSessionManager().isSessionAlive(p))
            {
                sender.sendMessage(getMessage("START_SESSION_NOT_SELF"));
            }
            else
            {
                int rememberLoginFor = getConfig().getInt("login-sessions.valid-for");
                int currentTime = (int) (System.currentTimeMillis() / 1000L);
                
                try
                {
                    getAccountManager().saveLoginSession(p.getName(),
                            PlayerUtils.getPlayerIp(p), currentTime);
                    
                    sender.sendMessage(getMessage("REMEMBER_SUCCESS").replace("%sec%",
                            String.valueOf(rememberLoginFor)));
                }
                catch (IOException ex)
                {
                    log(Level.WARNING, ex);
                    sender.sendMessage(getMessage("REMEMBER_FAIL"));
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
