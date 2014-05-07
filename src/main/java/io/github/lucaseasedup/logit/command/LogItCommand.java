/*
 * LogItCommand.java
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
import io.github.lucaseasedup.logit.FatalReportedException;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.command.wizard.ConvertWizard;
import io.github.lucaseasedup.logit.config.InvalidPropertyValueException;
import io.github.lucaseasedup.logit.config.LocationSerializable;
import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.Location;
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
        
        if (checkSubcommand(args, "help", 0))
        {
            if (!checkPermission(p, "logit.help"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                subcommandHelp(sender, p);
            }
        }
        else if (checkSubcommand(args, "version", 0))
        {
            if (!checkPermission(p, "logit.version"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else
            {
                subcommandVersion(sender);
            }
        }
        else if (checkSubcommand(args, "start", 0))
        {
            if (!checkPermission(p, "logit.start"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_ALREADY_STARTED"));
            }
            else
            {
                subcommandStart(sender);
            }
        }
        else if (checkSubcommand(args, "stop", 0))
        {
            if (!checkPermission(p, "logit.stop"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandStop(sender);
            }
        }
        else if (checkSubcommand(args, "reload", 0))
        {
            if (!checkPermission(p, "logit.reload"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandReload(p);
            }
        }
        else if (checkSubcommand(args, "backup force", 0))
        {
            if (!checkPermission(p, "logit.backup.force"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandBackupForce(p);
            }
        }
        else if (checkSubcommand(args, "backup restore", 1))
        {
            if (!checkPermission(p, "logit.backup.restore"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandBackupRestore(p, (args.length >= 3) ? args[2] : null);
            }
        }
        else if (checkSubcommand(args, "backup remove", 1))
        {
            if (!checkPermission(p, "logit.backup.remove"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "amount"));
            }
            else
            {
                subcommandBackupRemove(sender, args[2]);
            }
        }
        else if (checkSubcommand(args, "backup count", 0))
        {
            if (!checkPermission(p, "logit.backup.count"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandBackupCount(sender);
            }
        }
        else if (checkSubcommand(args, "gotowr", 0))
        {
            if (p == null)
            {
                sender.sendMessage(getMessage("ONLY_PLAYERS"));
            }
            else if (!p.hasPermission("logit.gotowr"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandGotowr(p);
            }
        }
        else if (checkSubcommand(args, "globalpass set", 1))
        {
            int minPasswordLength = getConfig().getInt("password.min-length");
            int maxPasswordLength = getConfig().getInt("password.max-length");
            
            if (!checkPermission(p, "logit.globalpass.set"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "password"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
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
                subcommandGlobalpassSet(p, args[2]);
            }
        }
        else if (checkSubcommand(args, "globalpass remove", 0))
        {
            if (!checkPermission(p, "logit.globalpass.remove"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandGlobalpassRemove(p);
            }
        }
        else if (checkSubcommand(args, "account count", 0))
        {
            if (!checkPermission(p, "logit.account.count"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandAccountCount(sender);
            }
        }
        else if (checkSubcommand(args, "account status", 1))
        {
            if (!checkPermission(p, "logit.account.status"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "username"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandAccountStatus(sender, args[2]);
            }
        }
        else if (checkSubcommand(args, "ipcount", 1))
        {
            if (!checkPermission(p, "logit.ipcount"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandIpcount(sender, (args.length >= 2) ? args[1] : null);
            }
        }
        else if (checkSubcommand(args, "config set"))
        {
            if (!checkPermission(p, "logit.config.set"))
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
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else if (!getConfig().contains(args[2]))
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_NOT_FOUND")
                        .replace("%param%", "path"));
            }
            else
            {
                subcommandConfigSet(sender, args);
            }
        }
        else if (checkSubcommand(args, "config get", 1))
        {
            if (!checkPermission(p, "logit.config.get"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (args.length < 3)
            {
                sender.sendMessage(getMessage("PARAM_MISSING").replace("%param%", "path"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else if (!getConfig().contains(args[2]))
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_NOT_FOUND")
                        .replace("%param%", "path"));
            }
            else
            {
                subcommandConfigGet(sender, args[2]);
            }
        }
        else if (checkSubcommand(args, "config list", 1))
        {
            if (!checkPermission(p, "logit.config.list"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandConfigList(sender, (args.length >= 3) ? args[2] : null);
            }
        }
        else if (checkSubcommand(args, "config reload", 0))
        {
            if (!checkPermission(p, "logit.config.reload"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandConfigReload(sender);
            }
        }
        else if (checkSubcommand(args, "convert", 0))
        {
            if (!checkPermission(p, "logit.convert"))
            {
                sender.sendMessage(getMessage("NO_PERMS"));
            }
            else if (!isCoreStarted())
            {
                sender.sendMessage(getMessage("CORE_NOT_STARTED"));
            }
            else
            {
                subcommandConvert(sender, args);
            }
        }
        else
        {
            if (!checkPermission(p, "logit"))
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
    
    private void subcommandHelp(CommandSender sender, Player p)
    {
        if (checkPermission(p, "logit.help"))
        {
            sender.sendMessage(buildSubcommandHelp("help", null));
        }
        if (checkPermission(p, "logit.version"))
        {
            sender.sendMessage(buildSubcommandHelp("version", null));
        }
        if (checkPermission(p, "logit.reload"))
        {
            sender.sendMessage(buildSubcommandHelp("reload", null));
        }
        if (checkPermission(p, "logit.backup.force"))
        {
            sender.sendMessage(buildSubcommandHelp("backup force", null));
        }
        if (checkPermission(p, "logit.backup.restore"))
        {
            sender.sendMessage(buildSubcommandHelp("backup restore", "[filename]"));
        }
        if (checkPermission(p, "logit.backup.remove"))
        {
            sender.sendMessage(buildSubcommandHelp("backup remove", "<amount>"));
        }
        if (checkPermission(p, "logit.backup.count"))
        {
            sender.sendMessage(buildSubcommandHelp("backup count", null));
        }
        if (checkPermission(p, "logit.gotowr"))
        {
            sender.sendMessage(buildSubcommandHelp("gotowr", null));
        }
        if (checkPermission(p, "logit.globalpass.set"))
        {
            sender.sendMessage(buildSubcommandHelp("globalpass set", "<password>"));
        }
        if (checkPermission(p, "logit.globalpass.remove"))
        {
            sender.sendMessage(buildSubcommandHelp("globalpass remove", null));
        }
        if (checkPermission(p, "logit.account.count"))
        {
            sender.sendMessage(buildSubcommandHelp("account count", null));
        }
        if (checkPermission(p, "logit.account.status"))
        {
            sender.sendMessage(buildSubcommandHelp("account status", "<username>"));
        }
        if (checkPermission(p, "logit.ipcount"))
        {
            sender.sendMessage(buildSubcommandHelp("ipcount", "[ip]"));
        }
        if (checkPermission(p, "logit.config.get"))
        {
            sender.sendMessage(buildSubcommandHelp("config get", "<path>"));
        }
        if (checkPermission(p, "logit.config.set"))
        {
            sender.sendMessage(buildSubcommandHelp("config set", "<path> <value>"));
        }
        if (checkPermission(p, "logit.config.list"))
        {
            sender.sendMessage(buildSubcommandHelp("config list", "[page]"));
        }
        if (checkPermission(p, "logit.config.reload"))
        {
            sender.sendMessage(buildSubcommandHelp("config reload", null));
        }
        if (checkPermission(p, "logit.convert"))
        {
            sender.sendMessage(buildSubcommandHelp("convert", null));
        }
    }
    
    private void subcommandVersion(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            sender.sendMessage("");
        }
        
        sender.sendMessage(getMessage("PLUGIN_ABOUT"));
        sender.sendMessage(getMessage("PLUGIN_VERSION"));
        sender.sendMessage(getMessage("PLUGIN_AUTHOR"));
    }
    
    private void subcommandStart(CommandSender sender)
    {
        try
        {
            getCore().start();
            
            sender.sendMessage(getMessage("CORE_STARTED"));
        }
        catch (FatalReportedException ex)
        {
            sender.sendMessage(getMessage("COULD_NOT_START_CORE"));
        }
    }
    
    private void subcommandStop(CommandSender sender)
    {
        getCore().stop();
        
        sender.sendMessage(getMessage("CORE_STOPPED"));
    }
    
    private void subcommandReload(Player player)
    {
        try
        {
            getCore().restart();
            
            if (player != null)
            {
                player.sendMessage(getMessage("RELOADED"));
            }
        }
        catch (FatalReportedException ex)
        {
        }
    }
    
    private void subcommandBackupForce(Player player)
    {
        try
        {
            File backupFile = getBackupManager().createBackup();
            
            if (player != null)
            {
                player.sendMessage(getMessage("CREATE_BACKUP_SUCCESS")
                        .replace("%filename%", backupFile.getName()));
            }
            
            log(Level.INFO, getMessage("CREATE_BACKUP_SUCCESS")
                    .replace("%filename%", backupFile.getName()));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, getMessage("CREATE_BACKUP_FAIL"), ex);
        }
    }
    
    private void subcommandBackupRestore(Player player, String filename)
    {
        try
        {
            ReportedException.incrementRequestCount();
            
            if (filename == null)
            {
                File[] backups = getBackupManager().getBackups(true);
                
                if (backups.length == 0)
                    throw new FileNotFoundException();
                
                filename = backups[backups.length - 1].getName();
            }
            
            getBackupManager().restoreBackup(filename);
            
            if (player != null)
            {
                player.sendMessage(getMessage("RESTORE_BACKUP_SUCCESS")
                        .replace("%filename%", filename));
            }
        }
        catch (ReportedException | FileNotFoundException ex)
        {
            if (player != null)
            {
                player.sendMessage(getMessage("RESTORE_BACKUP_FAIL"));
            }
        }
        finally
        {
            ReportedException.decrementRequestCount();
        }
    }
    
    private void subcommandBackupRemove(CommandSender sender, String amountString)
    {
        try
        {
            int amount = Integer.parseInt(amountString);
            
            getBackupManager().removeBackups(amount);
            
            if (sender instanceof Player)
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
    
    private void subcommandBackupCount(CommandSender sender)
    {
        int backupCount = getBackupManager().getBackups(false).length;
        
        sender.sendMessage(getMessage("BACKUP_COUNT")
                .replace("%count%", String.valueOf(backupCount)));
    }
    
    private void subcommandGotowr(Player player)
    {
        player.teleport(getConfig().getLocation("waiting-room.location").toBukkitLocation());
    }
    
    private void subcommandGlobalpassSet(Player player, String password)
    {
        getCore().changeGlobalPassword(password);
        
        if (player != null)
        {
            player.sendMessage(getMessage("GLOBALPASS_SET_SUCCESS"));
        }
    }
    
    private void subcommandGlobalpassRemove(Player player)
    {
        getCore().removeGlobalPassword();
        
        if (player != null)
        {
            player.sendMessage(getMessage("GLOBALPASS_REMOVE_SUCCESS"));
        }
    }
    
    private void subcommandAccountCount(CommandSender sender)
    {
        try
        {
            sender.sendMessage(getMessage("ACCOUNT_COUNT")
                    .replace("%num%", String.valueOf(getAccountManager().getAccountCount())));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
        }
    }
    
    private void subcommandAccountStatus(CommandSender sender, String username)
    {
        StringBuilder status = new StringBuilder(); 
        
        if (getAccountManager().isRegistered(username))
        {
            status.append(getMessage("ACCOUNT_STATUS_REGISTERED"));
        }
        else
        {
            status.append(getMessage("ACCOUNT_STATUS_NOT_REGISTERED"));
        }
        
        sender.sendMessage(getMessage("ACCOUNT_STATUS")
                .replace("%username%", username)
                .replace("%status%", status.toString()));
    }
    
    private void subcommandIpcount(CommandSender sender, String ip)
    {
        if (ip == null)
        {
            sender.sendMessage(getMessage("IP_COUNT_UNIQUE")
                .replace("%num%", String.valueOf(getAccountManager().countUniqueIps())));
        }
        else
        {
            sender.sendMessage(getMessage("IP_COUNT_ACCOUNTS")
                .replace("%ip%", ip)
                .replace("%num%", String.valueOf(getAccountManager().countAccountsWithIp(ip))));
        }
    }
    
    private void subcommandConfigSet(CommandSender sender, String[] args)
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
            case CONFIGURATION_SECTION:
            case OBJECT:
                throw new Exception("Unsupported property type conversion.");
                
            case BOOLEAN:
                outputValue = Boolean.valueOf(inputValue);
                break;
                
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
                    
                    try
                    {
                        outputValue = Color.fromRGB(Integer.parseInt(rgb[0]),
                                                    Integer.parseInt(rgb[1]),
                                                    Integer.parseInt(rgb[2]));
                    }
                    catch (NumberFormatException ex)
                    {
                        sender.sendMessage(getMessage("INVALID_PARAMETER")
                                .replace("%param%", "value"));
                        
                        return;
                    }
                }
                }
                
                break;
            }
            
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
            {
                if ("$".equals(inputValue))
                {
                    if (!(sender instanceof Player))
                        throw new Exception(getMessage("ONLY_PLAYERS"));
                    
                    Player player = ((Player) sender);
                    
                    outputValue = new Vector(player.getLocation().getX(),
                                             player.getLocation().getY(),
                                             player.getLocation().getZ());
                }
                else
                {
                    String[] axes = inputValue.split(" ");
                    
                    if (axes.length != 3)
                        throw new Exception("Malformed vector representation.");
                    
                    try
                    {
                        outputValue = new Vector(Double.parseDouble(axes[0]),
                                                 Double.parseDouble(axes[1]),
                                                 Double.parseDouble(axes[2]));
                    }
                    catch (NumberFormatException ex)
                    {
                        sender.sendMessage(getMessage("INVALID_PARAMETER")
                                .replace("%param%", "value"));
                        
                        return;
                    }
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
                if ("$".equals(inputValue))
                {
                    if (!(sender instanceof Player))
                        throw new Exception(getMessage("ONLY_PLAYERS"));
                    
                    Location loc = ((Player) sender).getLocation();
                    
                    outputValue = new LocationSerializable(loc.getWorld().getName(),
                            loc.getX(), loc.getY(), loc.getZ(),
                            loc.getYaw(), loc.getPitch());
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
            
            if (sender instanceof Player)
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_SET_SUCCESS")
                        .replace("%path%", args[2])
                        .replace("%value%", getConfig().toString(args[2])));
            }
            
            if (getConfig().getProperty(args[2]).requiresRestart())
            {
                sender.sendMessage(getMessage("CONFIG_RELOAD_PLUGIN"));
            }
        }
        catch (Exception ex)
        {
            sender.sendMessage(getMessage("CONFIG_PROPERTY_SET_FAIL")
                    .replace("%cause%", ex.getMessage()));
        }
    }
    
    private void subcommandConfigGet(CommandSender sender, String path)
    {
        sender.sendMessage(getMessage("CONFIG_PROPERTY_GET")
                .replace("%path%", path)
                .replace("%value%", getConfig().toString(path)));
    }
    
    private void subcommandConfigList(CommandSender sender, String pageString)
    {
        Map<String, Property> properties = getConfig().getProperties();
        
        final int PROPERTIES_PER_PAGE = 15;
        int page = 1;
        int pages = (properties.size() / PROPERTIES_PER_PAGE) + 1;
        int i = 0, j = 0;
        
        if (pageString != null)
        {
            try
            {
                page = Integer.parseInt(pageString);
            }
            catch (NumberFormatException ex)
            {
                sender.sendMessage(getMessage("INVALID_PARAMETER")
                        .replace("%param%", "page"));
                
                return;
            }
        }
        
        if (page <= 0)
        {
            page = 1;
        }
        
        if (sender instanceof Player)
        {
            sender.sendMessage("");
        }
        
        sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_HEADER")
                .replace("%page%", String.valueOf(page))
                .replace("%pages%", String.valueOf(pages)));
        sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_HEADER2"));
        sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_HEADER3"));
        
        for (Entry<String, Property> e : properties.entrySet())
        {
            if ((i > ((PROPERTIES_PER_PAGE * (page - 1)) - 1)) && (j < PROPERTIES_PER_PAGE))
            {
                sender.sendMessage(getMessage("CONFIG_PROPERTY_GET")
                        .replace("%path%", e.getValue().getPath())
                        .replace("%value%", e.getValue().toString()));
                
                j++;
            }
            
            i++;
        }
        
        if (page > pages)
        {
            sender.sendMessage(getMessage("CONFIG_PROPERTY_LIST_NO_PROPERTIES"));
        }
    }
    
    private void subcommandConfigReload(CommandSender sender)
    {
        try
        {
            getConfig().load();
            
            log(Level.INFO, getMessage("CONFIG_RELOAD_SUCCESS"));
            
            if (sender instanceof Player)
            {
                sender.sendMessage(getMessage("CONFIG_RELOAD_SUCCESS"));
            }
        }
        catch (IOException | InvalidPropertyValueException ex)
        {
            ex.printStackTrace();
            
            sender.sendMessage(getMessage("CONFIG_RELOAD_FAIL"));
        }
    }
    
    private void subcommandConvert(CommandSender sender, String[] args)
    {
        new ConvertWizard(sender).createWizard();
    }
    
    private boolean checkPermission(Player player, String permission)
    {
        return player == null || player.hasPermission(permission);
    }
    
    /**
     * Checks if a subcommand matches the input.
     * 
     * @param subcommand            subcommand.
     * @param maximumNumberOfParams maximum number of params.
     * 
     * @return {@code true} if the subcommand matches the input; {@code false} otherwise.
     */
    private boolean checkSubcommand(String[] args, String subcommand, int maximumNumberOfParams)
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
    private boolean checkSubcommand(String[] args, String subcommand)
    {
        return checkSubcommand(args, subcommand, Integer.MAX_VALUE);
    }
    
    private static String buildSubcommandHelp(String subcommand, String params)
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
        
        return line.replace("%desc%",
                getMessage("DESC_" + subcommand.replace(" ", "_").toUpperCase()));
    }
}
