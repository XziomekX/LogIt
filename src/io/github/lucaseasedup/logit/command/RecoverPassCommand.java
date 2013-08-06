/*
 * RecoverPassCommand.java
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
import io.github.lucaseasedup.logit.LogItCore;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public class RecoverPassCommand extends AbstractCommandExecutor
{
    public RecoverPassCommand(LogItCore core)
    {
        super(core);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        try
        {
            p = (Player) sender;
        }
        catch (ClassCastException ex)
        {
        }
        
        if (args.length <= 1)
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.recoverpass"))
            {
                p.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 1)
            {
                p.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "email"));
            }
            else if (!core.getAccountManager().isRegistered(p.getName()))
            {
                p.sendMessage(getMessage("CREATE_ACCOUNT_NOT_SELF"));
            }
            else if (!core.getAccountManager().getEmail(p.getName()).equals(args[0]))
            {
                p.sendMessage(getMessage("INCORRECT_EMAIL_ADDRESS"));
            }
            else
            {
                try
                {
                    core.sendPasswordRecoveryMail(p.getName());
                    sender.sendMessage(getMessage("RECOVER_PASSWORD_SUCCESS_SELF").replace("%email%", args[0]));
                }
                catch (IOException | SQLException ex)
                {
                    Logger.getLogger(RecoverPassCommand.class.getName()).log(Level.WARNING, null, ex);
                    sender.sendMessage(getMessage("RECOVER_PASSWORD_FAIL_SELF"));
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