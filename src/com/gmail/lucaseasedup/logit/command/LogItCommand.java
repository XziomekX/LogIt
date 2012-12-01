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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import static java.util.logging.Level.*;
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
        Player p = null;
        
        try
        {
            p = (Player) s;
        }
        catch (ClassCastException ex)
        {
        }
        
        String subcommand = (args.length > 0) ? args[0] : "";
        
        if (subcommand.equalsIgnoreCase("help") && args.length == 1)
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
            if (p == null || p.hasPermission("logit.backup.force"))
            {
                s.sendMessage(getLogItSubcommandHelp("backup force", null));
            }
            if (p == null || p.hasPermission("logit.backup.restore"))
            {
                s.sendMessage(getLogItSubcommandHelp("backup restore", "[filename]"));
            }
            if (p == null || p.hasPermission("logit.backup.remove"))
            {
                s.sendMessage(getLogItSubcommandHelp("backup remove", "<amount>"));
            }
            if (p != null && p.hasPermission("logit.setwr"))
            {
                s.sendMessage(getLogItSubcommandHelp("setwr", null));
            }
            if (p != null && p.hasPermission("logit.gotowr"))
            {
                s.sendMessage(getLogItSubcommandHelp("gotowr", null));
            }
            if (p == null || p.hasPermission("logit.globalpass.set"))
            {
                s.sendMessage(getLogItSubcommandHelp("globalpass set", "<password>"));
            }
            if (p == null || p.hasPermission("logit.globalpass.remove"))
            {
                s.sendMessage(getLogItSubcommandHelp("globalpass remove", null));
            }
            
            return true;
        }
        else if (subcommand.equalsIgnoreCase("version") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.version"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                
                return true;
            }
            
            s.sendMessage(getMessage("PLUGIN_VERSION").replace("%version%", core.getPlugin().getDescription().getVersion()));
            
            return true;
        }
        else if (subcommand.equalsIgnoreCase("reload") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.reload"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                
                return true;
            }
            
            core.restart();
            
            if (p != null && core.getPlugin().isEnabled())
                s.sendMessage(getMessage("RELOADED"));
            
            return true;
        }
        else if (subcommand.equalsIgnoreCase("purge") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.purge"))
            {
                s.sendMessage(getMessage("NO_PERMS"));
                
                return true;
            }
            
            try
            {
                core.getAccountManager().purge();
                
                if (p != null)
                {
                    s.sendMessage(getMessage("PURGE_SUCCESS"));
                }
            }
            catch (SQLException ex)
            {
                if (p != null)
                {
                    s.sendMessage(getMessage("PURGE_FAIL"));
                }
            }
            
            return true;
        }
        else if (subcommand.equalsIgnoreCase("backup") && args.length > 1 && args.length <= 3)
        {
            if (args[1].equalsIgnoreCase("force") && args.length == 2)
            {
                if (p != null && !p.hasPermission("logit.backup.force"))
                {
                    s.sendMessage(getMessage("NO_PERMS"));
                    
                    return true;
                }
                
                try
                {
                    core.getBackupManager().createBackup(core.getDatabase());
                    
                    if (p != null)
                        s.sendMessage(getMessage("CREATE_BACKUP_SUCCESS"));
                    
                    core.log(INFO, getMessage("CREATE_BACKUP_SUCCESS"));
                }
                catch (IOException|SQLException ex)
                {
                    if (p != null)
                        s.sendMessage(getMessage("CREATE_BACKUP_FAIL"));
                    
                    core.log(WARNING, getMessage("CREATE_BACKUP_FAIL"));
                }
            }
            else if (args[1].equalsIgnoreCase("restore"))
            {
                if (p != null && !p.hasPermission("logit.backup.restore"))
                {
                    s.sendMessage(getMessage("NO_PERMS"));
                    
                    return true;
                }
                
                if (args.length == 2)
                {
                    try
                    {
                        core.getBackupManager().restoreBackup(core.getDatabase());
                        core.getAccountManager().loadAccounts();

                        if (p != null)
                            s.sendMessage(getMessage("RESTORE_BACKUP_SUCCESS"));
                        
                        core.log(INFO, getMessage("RESTORE_BACKUP_SUCCESS"));
                    }
                    catch (FileNotFoundException|SQLException ex)
                    {
                        if (p != null)
                            s.sendMessage(getMessage("RESTORE_BACKUP_FAIL"));
                        
                        core.log(WARNING, getMessage("RESTORE_BACKUP_FAIL"));
                    }
                }
                else if (args.length == 3)
                {
                    try
                    {
                        core.getBackupManager().restoreBackup(core.getDatabase(), args[2]);
                        core.getAccountManager().loadAccounts();
                        
                        if (p != null)
                            s.sendMessage(getMessage("RESTORE_BACKUP_SUCCESS"));
                        
                        core.log(INFO, getMessage("RESTORE_BACKUP_SUCCESS"));
                    }
                    catch (FileNotFoundException|SQLException ex)
                    {
                        if (p != null)
                            s.sendMessage(getMessage("RESTORE_BACKUP_FAIL"));

                        core.log(WARNING, getMessage("RESTORE_BACKUP_FAIL"));
                    }
                }
            }
            else if (args[1].equalsIgnoreCase("remove"))
            {
                if (p != null && !p.hasPermission("logit.backup.remove"))
                {
                    s.sendMessage(getMessage("NO_PERMS"));
                    
                    return true;
                }
                if (args.length < 3)
                {
                    s.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "amount"));
                    
                    return true;
                }
                
                try
                {
                    core.getBackupManager().removeBackups(Integer.parseInt(args[2]));
                    
                    if (p != null)
                        s.sendMessage(getMessage("REMOVE_BACKUPS_SUCCESS"));

                    core.log(INFO, getMessage("REMOVE_BACKUPS_SUCCESS"));
                }
                catch (NumberFormatException|IOException ex)
                {
                    if (p != null)
                        s.sendMessage(getMessage("REMOVE_BACKUPS_FAIL"));

                    core.log(WARNING, getMessage("REMOVE_BACKUPS_FAIL"));
                }
            }
            
            return true;
        }
        else if (subcommand.equalsIgnoreCase("setwr") && args.length == 1)
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
        else if (subcommand.equalsIgnoreCase("gotowr") && args.length == 1)
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
        else if (subcommand.equalsIgnoreCase("globalpass") && args.length > 1 && args.length <= 3)
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
                    s.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%",
                            String.valueOf(core.getConfig().getPasswordMinLength())));
                    
                    return true;
                }
                if (args[2].length() > core.getConfig().getPasswordMaxLength())
                {
                    s.sendMessage(getMessage("PASSWORD_TOO_LONG").replace("%max-length%",
                            String.valueOf(core.getConfig().getPasswordMaxLength())));
                    
                    return true;
                }
                
                core.changeGlobalPassword(args[2]);
                
                if (p != null)
                    s.sendMessage(getMessage("GLOBALPASS_SET_SUCCESS"));
                
                return true;
            }
            else if (args[1].equalsIgnoreCase("remove") && args.length == 2)
            {
                if (p != null && !p.hasPermission("logit.globalpass.remove"))
                {
                    s.sendMessage(getMessage("NO_PERMS"));
                    
                    return true;
                }
                
                core.removeGlobalPassword();
                
                if (p != null)
                    s.sendMessage(getMessage("GLOBALPASS_REMOVE_SUCCESS"));
                
                return true;
            }
        }
        
        if (p != null && !p.hasPermission("logit"))
            s.sendMessage(getMessage("NO_PERMS"));
        else
            s.sendMessage(getMessage("TYPE_FOR_HELP"));
        
        return true;
    }
    
    private static String getLogItSubcommandHelp(String subcommand, String params)
    {
        String line = getMessage("CMD_HELP");
        
        if (params != null)
            line = line.replace("%cmd%", "logit " + subcommand + " " + params);
        else
            line = line.replace("%cmd%", "logit " + subcommand);
        
        return line.replace("%desc%", getMessage("DESC_" + subcommand.replace(" ", "_").toUpperCase()));
    }
    
    private final LogItCore core;
}