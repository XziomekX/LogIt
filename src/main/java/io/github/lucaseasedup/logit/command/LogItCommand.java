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
import io.github.lucaseasedup.logit.FatalReportedException;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.command.wizard.ConvertWizard;
import io.github.lucaseasedup.logit.config.Location;
import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyType;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class LogItCommand extends LogItCoreObject implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player p = null;
        
        if (sender instanceof Player)
        {
            p = (Player) sender;
        }
        
        this.args = args;
        
        if (checkSubcommand("help", 0))
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
                if (p != null && p.hasPermission("logit.gotowr"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("gotowr", null));
                }
                if (p == null || p.hasPermission("logit.globalpass.set"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("globalpass set", "<password>"));
                }
                if (p == null || p.hasPermission("logit.globalpass.remove"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("globalpass remove", null));
                }
                if (p == null || p.hasPermission("logit.account.count"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("account count", null));
                }
                if (p == null || p.hasPermission("logit.account.status"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("account status", "<username>"));
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
                if (p == null || p.hasPermission("logit.config.list"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("config list", "[page]"));
                }
                if (p == null || p.hasPermission("logit.convert"))
                {
                    sender.sendMessage(getLogItSubcommandHelp("convert", null));
                }
            }
        }
        else if (checkSubcommand("version", 0))
        {
            if (p != null && !p.hasPermission("logit.version"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                if (sender instanceof Player)
                {
                    sender.sendMessage("");
                }
                
                sender.sendMessage(getMessage("PLUGIN_ABOUT"));
                sender.sendMessage(getMessage("PLUGIN_VERSION")
                        .replace("%version%", getPlugin().getDescription().getVersion()));
                sender.sendMessage(getMessage("PLUGIN_AUTHOR"));
            }
        }
        else if (checkSubcommand("reload", 0))
        {
            if (p != null && !p.hasPermission("logit.reload"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                try
                {
                    getCore().restart();
                }
                catch (FatalReportedException ex)
                {
                    return true;
                }
                
                if (p != null && getPlugin().isEnabled())
                {
                    sender.sendMessage(getMessage("RELOADED"));
                }
            }
        }
        else if (checkSubcommand("backup force", 0))
        {
            if (p != null && !p.hasPermission("logit.backup.force"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getBackupManager().createBackup();

                    if (p != null)
                    {
                        sender.sendMessage(getMessage("CREATE_BACKUP_SUCCESS"));
                    }
                    
                    log(Level.INFO, getMessage("CREATE_BACKUP_SUCCESS"));
                }
                catch (ReportedException ex)
                {
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("CREATE_BACKUP_FAIL"));
                    }
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if (checkSubcommand("backup restore", 0))
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
                    ReportedException.incrementRequestCount();
                    
                    if (filename != null)
                    {
                        getBackupManager().restoreBackup(filename);
                    }
                    else
                    {
                        File[] backups = getBackupManager().getBackups(true);
                        
                        if (backups.length == 0)
                            throw new FileNotFoundException();
                        
                        getBackupManager().restoreBackup(backups[backups.length - 1].getName());
                    }
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("RESTORE_BACKUP_SUCCESS"));
                    }
                    
                    log(Level.INFO, getMessage("RESTORE_BACKUP_SUCCESS"));
                }
                catch (ReportedException | FileNotFoundException ex)
                {
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("RESTORE_BACKUP_FAIL"));
                    }
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }
        else if (checkSubcommand("backup remove", 1))
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
                    int amount = Integer.parseInt(args[2]);
                    
                    getBackupManager().removeBackups(amount);
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("REMOVE_BACKUPS_SUCCESS"));
                    }
                }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(getMessage("INVALID_PARAMETER")
                            .replace("%param%", "amount"));
                }
            }
        }
        else if (checkSubcommand("gotowr", 0))
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
                p.teleport(getConfig().getLocation("waiting-room.location").toBukkitLocation());
            }
        }
        else if (checkSubcommand("globalpass set", 1))
        {
            int minPasswordLength = getConfig().getInt("password.min-length");
            int maxPasswordLength = getConfig().getInt("password.max-length");
            
            if (p != null && !p.hasPermission("logit.globalpass.set"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (args[2].length() < minPasswordLength)
            {
                sender.sendMessage(getMessage("PASSWORD_TOO_SHORT")
                        .replace("%min-length%", String.valueOf(minPasswordLength)));
            }
            else if (args[2].length() > maxPasswordLength)
            {
                sender.sendMessage(getMessage("PASSWORD_TOO_LONG")
                        .replace("%max-length%", String.valueOf(maxPasswordLength)));
            }
            else
            {
                getCore().changeGlobalPassword(args[2]);
                
                if (p != null)
                {
                    sender.sendMessage(getMessage("GLOBALPASS_SET_SUCCESS"));
                }
            }
        }
        else if (checkSubcommand("globalpass remove", 0))
        {
            if (p != null && !p.hasPermission("logit.globalpass.remove"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                getCore().removeGlobalPassword();
                
                if (p != null)
                {
                    sender.sendMessage(getMessage("GLOBALPASS_REMOVE_SUCCESS"));
                }
            }
        }
        else if (checkSubcommand("account count", 0))
        {
            if (p != null && !p.hasPermission("logit.account.count"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                sender.sendMessage(getMessage("ACCOUNT_COUNT")
                        .replace("%num%", String.valueOf(getAccountManager().getAccountCount())));
            }
        }
        else if (checkSubcommand("account status", 1))
        {
            if (p != null && !p.hasPermission("logit.account.status"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "username"));
            }
            else
            {
                StringBuilder status = new StringBuilder(); 
                
                if (getAccountManager().isRegistered(args[2]))
                {
                    status.append(getMessage("ACCOUNT_STATUS_REGISTERED"));
                }
                else
                {
                    status.append(getMessage("ACCOUNT_STATUS_NOT_REGISTERED"));
                }
                
                sender.sendMessage(getMessage("ACCOUNT_STATUS")
                        .replace("%username%", args[2])
                        .replace("%status%", status.toString()));
            }
        }
        else if (checkSubcommand("ipcount", 1))
        {
            if (p != null && !p.hasPermission("logit.ipcount"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length == 1)
            {
                sender.sendMessage(getMessage("IP_COUNT_UNIQUE")
                    .replace("%num%", String.valueOf(getAccountManager().countUniqueIps())));
            }
            else if (args.length == 2)
            {
                sender.sendMessage(getMessage("IP_COUNT_ACCOUNTS")
                    .replace("%ip%", args[1])
                    .replace("%num%", String.valueOf(getAccountManager().countAccountsWithIp(args[1]))));
            }
        }
        else if (checkSubcommand("config set"))
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
            else if (!getConfig().contains(args[2]))
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_NOT_FOUND")
                        .replace("%param%", "path"));
            }
            else
            {
                PropertyType type = getConfig().getType(args[2]);
                String inputValue = "";
                Object outputValue = null;
                
                for (int i = 3; i < args.length; i++)
                {
                    if (!inputValue.isEmpty())
                    {
                        inputValue += " ";
                    }
                    
                    inputValue += args[i];
                }
                
                try
                {
                    switch (type)
                    {
                    case OBJECT:
                        throw new Exception("Unsupported property type conversion.");
                    case BOOLEAN: outputValue = Boolean.valueOf(inputValue); break;
                    case COLOR:
                    {
                        switch (inputValue.toLowerCase())
                        {
                        case "aqua":    outputValue = Color.AQUA;    break;
                        case "black":   outputValue = Color.BLACK;   break;
                        case "blue":    outputValue = Color.BLUE;    break;
                        case "fuchsia": outputValue = Color.FUCHSIA; break;
                        case "gray":    outputValue = Color.GRAY;    break;
                        case "green":   outputValue = Color.GREEN;   break;
                        case "lime":    outputValue = Color.LIME;    break;
                        case "maroon":  outputValue = Color.MAROON;  break;
                        case "navy":    outputValue = Color.NAVY;    break;
                        case "olive":   outputValue = Color.OLIVE;   break;
                        case "orange":  outputValue = Color.ORANGE;  break;
                        case "purple":  outputValue = Color.PURPLE;  break;
                        case "red":     outputValue = Color.RED;     break;
                        case "silver":  outputValue = Color.SILVER;  break;
                        case "teal":    outputValue = Color.TEAL;    break;
                        case "white":   outputValue = Color.WHITE;   break;
                        case "yellow":  outputValue = Color.YELLOW;  break;
                        default:
                            {
                                String[] rgb = inputValue.split(" ");
                                
                                if (rgb.length != 3)
                                    throw new Exception("Malformed color representation.");
                                
                                outputValue = Color.fromRGB(Integer.valueOf(rgb[0]),
                                                            Integer.valueOf(rgb[1]),
                                                            Integer.valueOf(rgb[2]));
                                
                                break;
                            }
                        }
                        
                        break;
                    }
                    case DOUBLE: outputValue = Double.valueOf(inputValue);  break;
                    case INT:    outputValue = Integer.valueOf(inputValue); break;
                    case ITEM_STACK:
                        throw new Exception("Unsupported property type conversion.");
                    case LONG:   outputValue = Long.valueOf(inputValue);    break;
                    case STRING: outputValue = inputValue;                  break;
                    case VECTOR:
                    {
                        if (inputValue.equals("$"))
                        {
                            if (p != null)
                            {
                                outputValue = new Vector(p.getLocation().getX(),
                                                         p.getLocation().getY(),
                                                         p.getLocation().getZ());
                            }
                            else
                            {
                                throw new Exception(getMessage("ONLY_PLAYERS"));
                            }
                        }
                        else
                        {
                            String[] axes = inputValue.split(" ");
                            
                            if (axes.length != 3)
                                throw new Exception("Malformed vector representation.");
                            
                            outputValue = new Vector(Double.valueOf(axes[0]),
                                                     Double.valueOf(axes[1]),
                                                     Double.valueOf(axes[2]));
                        }
                        
                        break;
                    }
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
                    case LOCATION:
                    {
                        if (inputValue.equals("$"))
                        {
                            if (p != null)
                            {
                                outputValue = new Location(p.getLocation().getWorld().getName(),
                                                           p.getLocation().getX(),
                                                           p.getLocation().getY(),
                                                           p.getLocation().getZ(),
                                                           p.getLocation().getYaw(),
                                                           p.getLocation().getPitch());
                            }
                            else
                            {
                                throw new Exception(getMessage("ONLY_PLAYERS"));
                            }
                        }
                        else
                        {
                            throw new Exception("Unsupported property type conversion.");
                        }
                        
                        break;
                    }
                    default:
                        throw new Exception("Unknown property type.");
                    }
                    
                    getConfig().set(args[2], outputValue);
                    
                    if (p != null)
                    {
                        sender.sendMessage(getMessage("CONFIG_PROPERTY_SET_SUCCESS", new String[]{
                            "%path%", args[2],
                            "%value%", getConfig().toString(args[2]),
                        }));
                    }
                    
                    if (getConfig().getProperty(args[2]).requiresRestart())
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
        else if (checkSubcommand("config get", 1))
        {
            if (p != null && !p.hasPermission("logit.config.get"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "path"));
            }
            else if (!getConfig().contains(args[2]))
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_NOT_FOUND")
                        .replace("%param%", "path"));
            }
            else
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_GET", new String[]{
                    "%path%", args[2],
                    "%value%", getConfig().toString(args[2]),
                }));
            }
        }
        else if (checkSubcommand("config list", 1))
        {
            if (p != null && !p.hasPermission("logit.config.list"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                Map<String, Property> properties = getConfig().getProperties();
                
                final int PROPERTIES_PER_PAGE = 16;
                int page = 1;
                int pages = (int) Math.floor(properties.size() / PROPERTIES_PER_PAGE) + 1;
                int i = 0, j = 0;
                
                if (args.length >= 3)
                {
                    page = Integer.valueOf(args[2]);
                }
                
                if (page <= 0)
                {
                    page = 1;
                }
                
                sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_HEADER", new String[]{
                    "%page%", String.valueOf(page),
                    "%pages%", String.valueOf(pages),
                }));
                
                for (Entry<String, Property> e : properties.entrySet())
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
        else if (checkSubcommand("convert", 0))
        {
            if (p != null && !p.hasPermission("logit.convert"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                new ConvertWizard(sender, args).createWizard();
            }
        }
        else
        {
            if (p != null && !p.hasPermission("logit"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                sender.sendMessage(getMessage("TYPE_FOR_HELP"));
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a subcommand matches the input.
     * 
     * @param subcommand            subcommand.
     * @param maximumNumberOfParams maximum number of params.
     * @return {@code true} if the subcommand matches the input; {@code false} otherwise.
     */
    private boolean checkSubcommand(String subcommand, int maximumNumberOfParams)
    {
        if (args == null)
            return false;
        
        String[] words = subcommand.split("\\s+");
        
        if (words.length > args.length)
            return false;
        
        for (int i = 0; i < words.length; i++)
        {
            if (!words[i].equalsIgnoreCase(args[i]))
            {
                return false;
            }
        }
        
        return (args.length - words.length) <= maximumNumberOfParams;
    }
    
    /**
     * Checks if a subcommand matches the input.
     * 
     * <p> Equal to {@code checkSubcommand(command, Integer.MAX_VALUE)}.
     * 
     * @param subcommand subcommand.
     * @return {@code true} if the subcommand matches the input; {@code false} otherwise.
     */
    private boolean checkSubcommand(String subcommand)
    {
        return checkSubcommand(subcommand, Integer.MAX_VALUE);
    }
    
    private static String getLogItSubcommandHelp(String subcommand, String params)
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
        
        return line.replace("%desc%", getMessage("DESC_" + subcommand.replace(" ", "_").toUpperCase()));
    }
    
    private String[] args = null;
}
