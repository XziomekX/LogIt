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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
                if (p == null || p.hasPermission("logit.config.get"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("config get", "<path>"));
                }
                if (p == null || p.hasPermission("logit.config.set"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("config set", "<path> <value>"));
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
                        
                        core.log(Level.INFO, getMessage("CREATE_BACKUP_SUCCESS"));
                    }
                    catch (IOException | SQLException ex)
                    {
                        Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                        
                        if (p != null)
                            sender.sendMessage(getMessage("CREATE_BACKUP_FAIL"));
                        
                        core.log(Level.WARNING, getMessage("CREATE_BACKUP_FAIL"));
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
                        
                        core.log(Level.INFO, getMessage("RESTORE_BACKUP_SUCCESS"));
                    }
                    catch (FileNotFoundException | SQLException ex)
                    {
                        Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                        
                        if (p != null)
                            sender.sendMessage(getMessage("RESTORE_BACKUP_FAIL"));
                        
                        core.log(Level.WARNING, getMessage("RESTORE_BACKUP_FAIL"));
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

                        core.log(Level.INFO, getMessage("REMOVE_BACKUPS_SUCCESS"));
                    }
                    catch (NumberFormatException | IOException ex)
                    {
                        Logger.getLogger(LogItCommand.class.getName()).log(Level.WARNING, null, ex);
                        
                        if (p != null)
                            sender.sendMessage(getMessage("REMOVE_BACKUPS_FAIL"));

                        core.log(Level.WARNING, getMessage("REMOVE_BACKUPS_FAIL"));
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
                core.getWaitingRoom().setWaitingRoomLocation(p.getLocation());
                core.getConfig().save();
                
                p.sendMessage(getMessage("WAITING_ROOM_SET"));
                core.log(Level.INFO, getMessage("WAITING_ROOM_SET"));
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
                p.teleport(core.getWaitingRoom().getWaitingRoomLocation());
            }
        }
        else if (subcommand.equalsIgnoreCase("globalpass") && args.length <= 3)
        {
            if (args.length >= 2 && args[1].equalsIgnoreCase("set"))
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
            else if (args.length >= 2 && args[1].equalsIgnoreCase("remove") && args.length == 2)
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
            else
            {
                sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
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
        else if (subcommand.equalsIgnoreCase("config"))
        {
            if (args.length >= 2 && args[1].equalsIgnoreCase("set"))
            {
                if (p != null && !p.hasPermission("logit.config.set"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else if (args.length < 3)
                {
                    sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "path"));
                }
                else if (args.length < 4)
                {
                    sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "value"));
                }
                else if (!core.getConfig().contains(args[2]))
                {
                    sender.sendMessage(getMessage("CONFIG_PROPERTY_NOT_FOUND").replace("%param%", "path"));
                }
                else
                {
                    PropertyType type = core.getConfig().getType(args[2]);
                    String inputValue = "";
                    Object outputValue = null;
                    
                    for (int i = 3; i < args.length; i++)
                    {
                        if (!inputValue.isEmpty())
                            inputValue += " ";
                        
                        inputValue += args[i];
                    }
                    
                    try
                    {
                        switch (type)
                        {
                        case OBJECT:
                            outputValue = inputValue;
                            break;
                        case BOOLEAN:
                            outputValue = Boolean.valueOf(inputValue);
                            break;
                        case COLOR:
                            if (inputValue.equalsIgnoreCase("aqua"))
                                outputValue = Color.AQUA;
                            else if (inputValue.equalsIgnoreCase("black"))
                                outputValue = Color.BLACK;
                            else if (inputValue.equalsIgnoreCase("blue"))
                                outputValue = Color.BLUE;
                            else if (inputValue.equalsIgnoreCase("fuchsia"))
                                outputValue = Color.FUCHSIA;
                            else if (inputValue.equalsIgnoreCase("gray"))
                                outputValue = Color.GRAY;
                            else if (inputValue.equalsIgnoreCase("green"))
                                outputValue = Color.GREEN;
                            else if (inputValue.equalsIgnoreCase("lime"))
                                outputValue = Color.LIME;
                            else if (inputValue.equalsIgnoreCase("maroon"))
                                outputValue = Color.MAROON;
                            else if (inputValue.equalsIgnoreCase("navy"))
                                outputValue = Color.NAVY;
                            else if (inputValue.equalsIgnoreCase("olive"))
                                outputValue = Color.OLIVE;
                            else if (inputValue.equalsIgnoreCase("orange"))
                                outputValue = Color.ORANGE;
                            else if (inputValue.equalsIgnoreCase("purple"))
                                outputValue = Color.PURPLE;
                            else if (inputValue.equalsIgnoreCase("red"))
                                outputValue = Color.RED;
                            else if (inputValue.equalsIgnoreCase("silver"))
                                outputValue = Color.SILVER;
                            else if (inputValue.equalsIgnoreCase("teal"))
                                outputValue = Color.TEAL;
                            else if (inputValue.equalsIgnoreCase("white"))
                                outputValue = Color.WHITE;
                            else if (inputValue.equalsIgnoreCase("yellow"))
                                outputValue = Color.YELLOW;
                            else
                            {
                                String[] rgb = inputValue.split(" ");
                                
                                if (rgb.length != 3)
                                    throw new Exception("Malformed color representation.");
                                
                                outputValue = Color.fromRGB(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
                            }
                            break;
                        case DOUBLE:
                            outputValue = Double.valueOf(inputValue);
                            break;
                        case INT:
                            outputValue = Integer.valueOf(inputValue);
                            break;
                        case ITEM_STACK:
                            throw new Exception("Unsupported property type conversion.");
                        case LONG:
                            outputValue = Long.valueOf(inputValue);
                            break;
                        case STRING:
                            outputValue = inputValue;
                            break;
                        case VECTOR:
                            if (inputValue.equals("$") && p != null)
                            {
                                outputValue = new Vector(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
                            }
                            else
                            {
                                String[] axes = inputValue.split(" ");
                                
                                if (axes.length != 3)
                                    throw new Exception("Malformed vector representation.");
                                
                                outputValue = new Vector(Double.valueOf(axes[0]), Double.valueOf(axes[1]), Double.valueOf(axes[2]));
                            }
                            break;
                        case LIST:
                        case BOOLEAN_LIST:
                        case BYTE_LIST:
                        case CHARACTER_LIST:
                        case DOUBLE_LIST:
                        case FLOAT_LIST:
                        case INTEGER_LIST:
                        case LONG_LIST:
                        case MAP_LIST:
                        case SHORT_LIST:
                        case STRING_LIST:
                            throw new Exception("Unsupported property type conversion.");
                        default:
                            throw new Exception("Unknown property type.");
                        }
                        
                        core.getConfig().set(args[2], outputValue);
                        
                        if (p != null)
                        {
                            sender.sendMessage(getMessage("CONFIG_PROPERTY_SET_SUCCESS", new String[]{
                                "%path%", args[2],
                                "%value%", core.getConfig().toString(args[2]),
                            }));
                        }
                        
                        if (core.getConfig().getProperty(args[2]).changeRequiresRestart())
                        {
                            sender.sendMessage(getMessage("CONFIG_RELOAD_PLUGIN"));
                        }
                    }
                    catch (Exception ex)
                    {
                        sender.sendMessage(getMessage("CONFIG_PROPERTY_SET_FAIL", new String[]{
                            "%cause%", ex.getMessage()
                        }));
                    }
                }
            }
            else if (args.length >= 2 && args[1].equalsIgnoreCase("get") && args.length <= 3)
            {
                if (p != null && !p.hasPermission("logit.config.get"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else if (args.length < 3)
                {
                    sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "path"));
                }
                else if (!core.getConfig().contains(args[2]))
                {
                    sender.sendMessage(getMessage("CONFIG_PROPERTY_NOT_FOUND").replace("%param%", "path"));
                }
                else
                {
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("CONFIG_PROPERTY_GET", new String[]{
                            "%path%", args[2],
                            "%value%", core.getConfig().toString(args[2]),
                        }));
                    }
                }
            }
            else if (args.length >= 2 && args[1].equalsIgnoreCase("list") && args.length <= 3)
            {
                if (p != null && !p.hasPermission("logit.config.list"))
                {
                    sender.sendMessage(getMessage("NO_PERMS"));
                }
                else
                {
                    final int PROPERTIES_PER_PAGE = 16;
                    int page = 1;
                    int pages = (int) Math.floor(core.getConfig().getProperties().size() / PROPERTIES_PER_PAGE) + 1;
                    int i = 0, j = 0;
                    
                    if (args.length >= 3)
                        page = Integer.valueOf(args[2]);
                    
                    if (page <= 0)
                        page = 1;
                    
                    sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_HEADER", new String[]{
                        "%page%", String.valueOf(page),
                        "%pages%", String.valueOf(pages),
                    }));
                    
                    for (Map.Entry<String, Property> e : core.getConfig().getProperties().entrySet())
                    {
                        if ((i > ((PROPERTIES_PER_PAGE * (page - 1)) - 1)) && (j < PROPERTIES_PER_PAGE))
                        {
                            sender.sendMessage(getMessage("CONFIG_PROPERTY_GET", new String[]{
                                "%path%", e.getValue().getPath(),
                                "%value%", e.getValue().toString(),
                            }));
                            
                            j++;
                        }
                        
                        i++;
                    }
                    
                    if (page > pages)
                    {
                        sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_NO_PROPERTIES"));
                    }
                }
            }
            else
            {
                sender.sendMessage(getMessage("INCORRECT_PARAMETER_COMBINATION"));
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