/*
 * LogItCommand.java
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

import io.github.lucaseasedup.logit.LogItCore;
import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogItCommand extends AbstractCommandExecutor
{
    public LogItCommand(LogItCore core)
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
        
        String subcommand = (args.length > 0) ? args[0] : "";
        
        if (subcommand.equalsIgnoreCase("help") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.help"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                if (p == null || p.hasPermission("logit.help"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("help", null));
                }
                if (p == null || p.hasPermission("logit.version"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("version", null));
                }
                if (p == null || p.hasPermission("logit.reload"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("reload", null));
                }
                if (p == null || p.hasPermission("logit.purge"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("purge", null));
                }
                if (p == null || p.hasPermission("logit.backup.force"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("backup force", null));
                }
                if (p == null || p.hasPermission("logit.backup.restore"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("backup restore", "[filename]"));
                }
                if (p == null || p.hasPermission("logit.backup.remove"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("backup remove", "<amount>"));
                }
                if (p != null && p.hasPermission("logit.setwr"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("setwr", null));
                }
                if (p != null && p.hasPermission("logit.gotowr"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("gotowr", null));
                }
                if (p != null && p.hasPermission("logit.togglewr"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("togglewr", null));
                }
                if (p == null || p.hasPermission("logit.globalpass.set"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("globalpass set", "<password>"));
                }
                if (p == null || p.hasPermission("logit.globalpass.remove"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("globalpass remove", null));
                }
                if (p == null || p.hasPermission("logit.accountcount"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("accountcount", null));
                }
                if (p == null || p.hasPermission("logit.ipcount"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("ipcount", "[ip]"));
                }
            }
        }
        else if (subcommand.equalsIgnoreCase("version") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.version"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                sender.sendMessage(getMessage("PLUGIN_VERSION").replace("%version%", core.getPlugin().getDescription().getVersion()));
            }
        }
        else if (subcommand.equalsIgnoreCase("reload") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.reload"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                core.restart();
                
                if (p != null && core.getPlugin().isEnabled())
                {
                    sender.sendMessage(getMessage("RELOADED"));
                }
            }
        }
        else if (subcommand.equalsIgnoreCase("purge") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.purge"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                try
                {
                    core.getAccountManager().purge();
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("PURGE_SUCCESS"));
                    }
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("PURGE_FAIL"));
                    }
                }
            }
        }
        else if (subcommand.equalsIgnoreCase("backup") && args.length > 1 && args.length <= 3)
        {
            if (args[1].equalsIgnoreCase("force") && args.length == 2)
            {
                if (p != null && !p.hasPermission("logit.backup.force"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else
                {
                    try
                    {
                        core.getBackupManager().createBackup(core.getDatabase());

                        if (p != null)
                            sender.sendMessage(getMessage("CREATE_BACKUP_SUCCESS"));
                        
                        core.log(INFO, getMessage("CREATE_BACKUP_SUCCESS"));
                    }
                    catch (IOException | SQLException ex)
                    {
                        Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                        
                        if (p != null)
                            sender.sendMessage(getMessage("CREATE_BACKUP_FAIL"));
                        
                        core.log(WARNING, getMessage("CREATE_BACKUP_FAIL"));
                    }
                }
            }
            else if (args[1].equalsIgnoreCase("restore"))
            {
                if (p != null && !p.hasPermission("logit.backup.restore"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else
                {
                    String filename = (args.length >= 3) ? args[2] : null;
                    
                    try
                    {
                        if (filename != null)
                            core.getBackupManager().restoreBackup(core.getDatabase(), filename);
                        else
                            core.getBackupManager().restoreBackup(core.getDatabase());
                        
                        core.getAccountManager().loadAccounts();
                        
                        if (p != null)
                            sender.sendMessage(getMessage("RESTORE_BACKUP_SUCCESS"));
                        
                        core.log(INFO, getMessage("RESTORE_BACKUP_SUCCESS"));
                    }
                    catch (FileNotFoundException | SQLException ex)
                    {
                        Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                        
                        if (p != null)
                            sender.sendMessage(getMessage("RESTORE_BACKUP_FAIL"));
                        
                        core.log(WARNING, getMessage("RESTORE_BACKUP_FAIL"));
                    }
                }
            }
            else if (args[1].equalsIgnoreCase("remove"))
            {
                if (p != null && !p.hasPermission("logit.backup.remove"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else if (args.length < 3)
                {
                    sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "amount"));
                }
                else
                {
                    try
                    {
                        core.getBackupManager().removeBackups(Integer.parseInt(args[2]));

                        if (p != null)
                            sender.sendMessage(getMessage("REMOVE_BACKUPS_SUCCESS"));

                        core.log(INFO, getMessage("REMOVE_BACKUPS_SUCCESS"));
                    }
                    catch (NumberFormatException | IOException ex)
                    {
                        Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                        
                        if (p != null)
                            sender.sendMessage(getMessage("REMOVE_BACKUPS_FAIL"));

                        core.log(WARNING, getMessage("REMOVE_BACKUPS_FAIL"));
                    }
                }
            }
        }
        else if (subcommand.equalsIgnoreCase("setwr") && args.length == 1)
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.setwr"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                core.getWaitingRoom().setLocation(p.getLocation());
                core.getConfig().save();
                
                p.sendMessage(getMessage("WAITING_ROOM_SET"));
                core.log(INFO, getMessage("WAITING_ROOM_SET"));
            }
        }
        else if (subcommand.equalsIgnoreCase("gotowr") && args.length == 1)
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.gotowr"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                p.teleport(core.getWaitingRoom().getLocation());
            }
        }
        else if (subcommand.equalsIgnoreCase("togglewr") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.gotowr"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                if (core.getConfig().getBoolean("waiting-room.enabled"))
                {
                    core.getConfig().set("waiting-room.enabled", false);
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("WAITING_ROOM_DISABLED"));
                    }
                    
                    core.log(INFO, getMessage("WAITING_ROOM_DISABLED"));
                }
                else
                {
                    core.getConfig().set("waiting-room.enabled", true);
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("WAITING_ROOM_ENABLED"));
                    }
                    
                    core.log(INFO, getMessage("WAITING_ROOM_ENABLED"));
                }
            }
        }
        else if (subcommand.equalsIgnoreCase("globalpass") && args.length > 1 && args.length <= 3)
        {
            if (args[1].equalsIgnoreCase("set"))
            {
                if (p != null && !p.hasPermission("logit.globalpass.set"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else if (args.length < 3)
                {
                    sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
                }
                else if (args[2].length() < core.getConfig().getInt("password.min-length"))
                {
                    sender.sendMessage(getMessage("PASSWORD_TOO_SHORT").replace("%min-length%",
                            String.valueOf(core.getConfig().getInt("password.min-length"))));
                }
                else if (args[2].length() > core.getConfig().getInt("password.max-length"))
                {
                    sender.sendMessage(getMessage("PASSWORD_TOO_LONG").replace("%max-length%",
                            String.valueOf(core.getConfig().getInt("password.max-length"))));
                }
                else
                {
                    core.changeGlobalPassword(args[2]);
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("GLOBALPASS_SET_SUCCESS"));
                    }
                }
            }
            else if (args[1].equalsIgnoreCase("remove") && args.length == 2)
            {
                if (p != null && !p.hasPermission("logit.globalpass.remove"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else
                {
                    core.removeGlobalPassword();
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("GLOBALPASS_REMOVE_SUCCESS"));
                    }
                }
            }
        }
        else if (subcommand.equalsIgnoreCase("accountcount") && args.length == 1)
        {
            if (p != null && !p.hasPermission("logit.accountcount"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                sender.sendMessage(getMessage("ACCOUNT_COUNT").replace("%num%", String.valueOf(core.getAccountManager().getAccountCount())));
            }
        }
        else if (subcommand.equalsIgnoreCase("ipcount") && args.length <= 2)
        {
            if (p != null && !p.hasPermission("logit.ipcount"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                if (args.length == 1)
                {
                    sender.sendMessage(getMessage("IP_COUNT_UNIQUE")
                        .replace("%num%", String.valueOf(core.getAccountManager().countUniqueIps())));
                }
                else if (args.length == 2)
                {
                    sender.sendMessage(getMessage("IP_COUNT_ACCOUNTS")
                        .replace("%ip%", args[1])
                        .replace("%num%", String.valueOf(core.getAccountManager().countAccountsWithIp(args[1]))));
                }
            }
        }
        else
        {
            if (p != null && !p.hasPermission("logit"))
                sender.sendMessage(getMessage("NO_PERMS"));
            else
                sender.sendMessage(getMessage("TYPE_FOR_HELP"));
        }
        
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
}