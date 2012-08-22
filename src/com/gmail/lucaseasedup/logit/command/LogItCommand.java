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
import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import java.sql.SQLException;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
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
        
        if (args[0].equalsIgnoreCase("help") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.help"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            if (p == null || p.hasPermission("logit.help"))
            {
                s.sendMessage(getLogItSubcommandHelp("help", null));
            }
            if (p == null || p.hasPermission("logit.version"))
            {
                s.sendMessage(getLogItSubcommandHelp("version", null));
            }
            if (p == null || p.hasPermission("logit.reload"))
            {
                s.sendMessage(getLogItSubcommandHelp("reload", null));
            }
            if (p == null || p.hasPermission("logit.purge"))
            {
                s.sendMessage(getLogItSubcommandHelp("purge", null));
            }
            if (p != null && p.hasPermission("logit.setwr"))
            {
                s.sendMessage(getLogItSubcommandHelp("setwr", null));
            }
            if (p != null && p.hasPermission("logit.gotowr"))
            {
                s.sendMessage(getLogItSubcommandHelp("gotowr", null));
            }
            if (p == null || p.hasPermission("logit.globalpass"))
            {
                s.sendMessage(getLogItSubcommandHelp("globalpass", "<password>"));
            }
            if (p == null || p.hasPermission("logit.remglobalpass"))
            {
                s.sendMessage(getLogItSubcommandHelp("remglobalpass", null));
            }
            
            return true;
        }
        else if (args[0].equalsIgnoreCase("version") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.version"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                return true;
            }
            
            s.sendMessage(getMessage("PLUGIN_VERSION").replace("%version%", core.getPlugin().getDescription().getVersion()));
            
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
                
                core.log(WARNING, getMessage("FAILED_DB_PURGE"));
                
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
        else if (args[0].equalsIgnoreCase("globalpass") && args.length > 1 && args.length <= 3)
        {
            if (args[1].equalsIgnoreCase("set"))
            {
                if (p != null && !p.hasPermission("logit.globalpass.set"))
                {
                    s.sendMessage(getMessage("NO_PERMS"));
                    return true;
                }
                if (args.length < 3)
                {
                    s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
                    return true;
                }
                if (args[2].length() < core.getConfig().getPasswordMinLength())
                {
                    s.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%", String.valueOf(core.getConfig().getPasswordMinLength())));
                    return true;
                }

                core.getConfig().setGlobalPasswordHash(core.hash(args[2]));
                core.getConfig().save();

                if (p != null)
                    s.sendMessage(getMessage("GLOBALPASS_CHANGED"));

                core.log(INFO, getMessage("GLOBALPASS_CHANGED"));

                return true;
            }
            else if (args[1].equalsIgnoreCase("remove") && args.length == 2)
            {
                if (p != null && !p.hasPermission("logit.globalpass.remove"))
                {
                    s.sendMessage(getMessage("NO_PERMS"));
                    return true;
                }

                core.getConfig().setGlobalPasswordHash("");
                core.getConfig().save();

                if (p != null)
                    s.sendMessage(getMessage("GLOBALPASS_REMOVED"));

                core.log(INFO, getMessage("GLOBALPASS_REMOVED"));

                return true;
            }
        }
        
        if (p != null && !p.hasPermission("logit"))
        {
            s.sendMessage(getMessage("NO_PERMS"));
            return true;
        }

        s.sendMessage(getMessage("TYPE_FOR_HELP"));
        
        return true;
    }
    
    private String getLogItSubcommandHelp(String subcommand, String params)
    {
        String line = getMessage("CMD_HELP");
        
        if (params != null)
        {
            line = line.replace("%cmd%", "logit " + subcommand + " " + params);
        }
        else
        {
            line = line.replace("%cmd%", "logit " + subcommand);
        }
        
        return line.replace("%desc%", getMessage("DESC_" + subcommand.toUpperCase()));
    }
    
    private LogItCore core;
}