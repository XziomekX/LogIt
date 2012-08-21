/*
 * LogItCommand.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.command;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import java.sql.SQLException;
import static java.util.logging.Level.INFO;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogItCommand implements CommandExecutor
{
    public LogItCommand(LogItCore core)
    {
        this.core = core;
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
    {
        if (!cmd.getName().equalsIgnoreCase("logit"))
            return false;
        
        Player p = null;
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        if (args.length == 0 || args.length > 2)
        {
            if (p != null && !p.hasPermission("logit"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            s.sendMessage(getMessage("TYPE_FOR_HELP"));
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("help") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.help"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }

            if (p == null || p.hasPermission("logit.help"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit help").replace("%desc%", "Displays help for LogIt."));
            if (p == null || p.hasPermission("logit.version"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit version").replace("%desc%", "Shows current version of LogIt."));
            if (p == null || p.hasPermission("logit.reload"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit reload").replace("%desc%", "Reloads LogIt."));
            if (p == null || p.hasPermission("logit.purge"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit purge").replace("%desc%", "Purges the database."));
            if (p != null && p.hasPermission("logit.setwr"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit setwr").replace("%desc%", "Sets the waiting room to your position."));
            if (p != null && p.hasPermission("logit.gotowr"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit gotowr").replace("%desc%", "Teleports you to the waiting room."));
            if (p == null || p.hasPermission("logit.globalpass"))
                s.sendMessage(getMessage("CMD_HELP").replace("%cmd%", "/logit globalpass <password>").replace("%desc%", "Changes the global password."));
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("version") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.version"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            s.sendMessage(getMessage("PLUGIN_VERSION").replace("%version%", core.getVersion()));
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("reload") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.reload"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }

            core.restart();
            
            if (Bukkit.getPluginManager().isPluginEnabled("LogIt"))
            {
                if (p != null)
                    s.sendMessage(getMessage("RELOADED"));

                core.log(INFO, getMessage("RELOADED"));
            }
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("purge") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.purge"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            try
            {
                core.purge();
            }
            catch (SQLException ex)
            {
                if (p != null)
                    s.sendMessage(getMessage("FAILED_DB_PURGE"));
                
                core.log(INFO, getMessage("FAILED_DB_PURGE"));
                
                return true;
            }
            
            if (p != null)
                s.sendMessage(getMessage("DB_PURGED"));
            
            core.log(INFO, getMessage("DB_PURGED"));
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("setwr") && args.length == 1)
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (p != null && !p.hasPermission("logit.setwr"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            core.getConfig().setWaitingRoomLocation(p.getLocation());
            core.getConfig().save();
            
            p.sendMessage(getMessage("WAITING_ROOM_SET"));
            
            core.log(INFO, getMessage("WAITING_ROOM_SET"));
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("gotowr") && args.length == 1)
        {
            if (p == null)
            {
                s.sendMessage(getMessage("ONLY_PLAYERS"));
                return true;
            }
            if (p != null && !p.hasPermission("logit.gotowr"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            p.teleport(core.getConfig().getWaitingRoomLocation());
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("globalpass") && args.length <= 2)
        {
            if (p != null && !p.hasPermission("logit.globalpass"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            if (args.length < 2)
            {
                s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
                return true;
            }
            if (args[1].length() < core.getConfig().getPasswordMinLength())
            {
                s.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%", String.valueOf(core.getConfig().getPasswordMinLength())));
                return true;
            }
            
            core.getConfig().setGlobalPasswordHash(core.hash(args[1]));
            core.getConfig().save();
            
            if (p != null)
                s.sendMessage(getMessage("GLOBALPASS_CHANGED"));
            
            core.log(INFO, getMessage("GLOBALPASS_CHANGED"));
            
            return true;
        }
        
        if (true)
        {
            if (p != null && !p.hasPermission("logit"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            s.sendMessage(getMessage("TYPE_FOR_HELP"));
        }
        
        return true;
    }
    
    private LogItCore core;
}