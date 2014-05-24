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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.FatalReportedException;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.TimeString;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.command.wizard.ConfirmationWizard;
import io.github.lucaseasedup.logit.command.wizard.ConvertWizard;
import io.github.lucaseasedup.logit.config.InvalidPropertyValueException;
import io.github.lucaseasedup.logit.config.LocationSerializable;
import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
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
                sendMsg(sender, _("noPerms"));
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
                sendMsg(sender, _("noPerms"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (isCoreStarted())
            {
                sendMsg(sender, _("coreAlreadyStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandBackupForce(sender);
            }
        }
        else if (checkSubcommand(args, "backup restore", 0))
        {
            if (!checkPermission(p, "logit.backup.restore"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandBackupRestore(sender);
            }
        }
        else if (checkSubcommand(args, "backup restore file", 1))
        {
            if (!checkPermission(p, "logit.backup.restore"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 4)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "filename"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandBackupRestore_File(sender, args[3]);
            }
        }
        else if (checkSubcommand(args, "backup restore time", 1))
        {
            if (!checkPermission(p, "logit.backup.restore"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 4)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "time"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandBackupRestore_Time(sender, args[3]);
            }
        }
        else if (checkSubcommand(args, "backup remove", 1))
        {
            if (!checkPermission(p, "logit.backup.remove"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "amount"));
            }
            else
            {
                subcommandBackupRemove(sender, args[2]);
            }
        }
        else if (checkSubcommand(args, "gotowr", 0))
        {
            if (p == null)
            {
                sendMsg(sender, _("onlyForPlayers"));
            }
            else if (!p.hasPermission("logit.gotowr"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "password"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else if (args[2].length() < minPasswordLength)
            {
                sendMsg(sender, _("passwordTooShort")
                        .replace("{0}", String.valueOf(minPasswordLength)));
            }
            else if (args[2].length() > maxPasswordLength)
            {
                sendMsg(sender, _("passwordTooLong")
                        .replace("{0}", String.valueOf(maxPasswordLength)));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandGlobalpassRemove(p);
            }
        }
        else if (checkSubcommand(args, "account status", 1))
        {
            if (!checkPermission(p, "logit.account.status"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "username"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 2)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "ip"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandIpcount(sender, args[1]);
            }
        }
        else if (checkSubcommand(args, "config set"))
        {
            if (!checkPermission(p, "logit.config.set"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "path"));
            }
            else if (args.length < 4)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "value"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else if (!getConfig().contains(args[2]))
            {
                sendMsg(sender, _("config.propertyNotFound")
                        .replace("{0}", args[2]));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (args.length < 3)
            {
                sendMsg(sender, _("paramMissing")
                        .replace("{0}", "path"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else if (!getConfig().contains(args[2]))
            {
                sendMsg(sender, _("config.propertyNotFound")
                        .replace("{0}", args[2]));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
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
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandConvert(sender);
            }
        }
        else if (checkSubcommand(args, "stats", 0))
        {
            if (!checkPermission(p, "logit.stats"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else if (!isCoreStarted())
            {
                sendMsg(sender, _("coreNotStarted"));
            }
            else
            {
                subcommandStats(sender);
            }
        }
        else
        {
            if (!checkPermission(p, "logit"))
            {
                sendMsg(sender, _("noPerms"));
            }
            else
            {
                sendMsg(sender, _("typeForHelp"));
            }
        }
        
        return true;
    }
    
    private void subcommandHelp(CommandSender sender, Player p)
    {
        if (checkPermission(p, "logit.help"))
        {
            sendMsg(sender, buildSubcommandHelp("help", null,
                    "subCmdDesc.help"));
        }
        if (checkPermission(p, "logit.version"))
        {
            sendMsg(sender, buildSubcommandHelp("version", null,
                    "subCmdDesc.version"));
        }
        if (checkPermission(p, "logit.reload"))
        {
            sendMsg(sender, buildSubcommandHelp("reload", null,
                    "subCmdDesc.reload"));
        }
        if (checkPermission(p, "logit.backup.force"))
        {
            sendMsg(sender, buildSubcommandHelp("backup force", null,
                    "subCmdDesc.backup.force"));
        }
        if (checkPermission(p, "logit.backup.restore"))
        {
            sendMsg(sender, buildSubcommandHelp("backup restore", null,
                    "subCmdDesc.backup.restore.newest"));
            sendMsg(sender, buildSubcommandHelp("backup restore file", "<filename>",
                    "subCmdDesc.backup.restore.filename"));
            sendMsg(sender, buildSubcommandHelp("backup restore time", "<time>",
                    "subCmdDesc.backup.restore.time"));
        }
        if (checkPermission(p, "logit.backup.remove"))
        {
            sendMsg(sender, buildSubcommandHelp("backup remove", "<amount>",
                    "subCmdDesc.backup.remove"));
        }
        if (checkPermission(p, "logit.gotowr"))
        {
            sendMsg(sender, buildSubcommandHelp("gotowr", null,
                    "subCmdDesc.gotowr"));
        }
        if (checkPermission(p, "logit.globalpass.set"))
        {
            sendMsg(sender, buildSubcommandHelp("globalpass set", "<password>",
                    "subCmdDesc.globalpass.set"));
        }
        if (checkPermission(p, "logit.globalpass.remove"))
        {
            sendMsg(sender, buildSubcommandHelp("globalpass remove", null,
                    "subCmdDesc.globalpass.remove"));
        }
        if (checkPermission(p, "logit.account.status"))
        {
            sendMsg(sender, buildSubcommandHelp("account status", "<username>",
                    "subCmdDesc.account.status"));
        }
        if (checkPermission(p, "logit.ipcount"))
        {
            sendMsg(sender, buildSubcommandHelp("ipcount", "<ip>",
                    "subCmdDesc.ipcount"));
        }
        if (checkPermission(p, "logit.config.get"))
        {
            sendMsg(sender, buildSubcommandHelp("config get", "<path>",
                    "subCmdDesc.config.get"));
        }
        if (checkPermission(p, "logit.config.set"))
        {
            sendMsg(sender, buildSubcommandHelp("config set", "<path> <value>",
                    "subCmdDesc.config.set"));
        }
        if (checkPermission(p, "logit.config.list"))
        {
            sendMsg(sender, buildSubcommandHelp("config list", "[page]",
                    "subCmdDesc.config.list"));
        }
        if (checkPermission(p, "logit.config.reload"))
        {
            sendMsg(sender, buildSubcommandHelp("config reload", null,
                    "subCmdDesc.config.reload"));
        }
        if (checkPermission(p, "logit.convert"))
        {
            sendMsg(sender, buildSubcommandHelp("convert", null,
                    "subCmdDesc.convert"));
        }
        if (checkPermission(p, "logit.stats"))
        {
            sendMsg(sender, buildSubcommandHelp("stats", null,
                    "subCmdDesc.stats"));
        }
    }
    
    private void subcommandVersion(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, _("aboutPlugin.header"));
        sendMsg(sender, _("aboutPlugin.version")
                .replace("{0}", getPlugin().getDescription().getVersion()));
        sendMsg(sender, _("aboutPlugin.author"));
    }
    
    private void subcommandStart(CommandSender sender)
    {
        try
        {
            getCore().start();
            
            sendMsg(sender, _("startCore.success"));
        }
        catch (FatalReportedException ex)
        {
            sendMsg(sender, _("startCore.fail"));
        }
    }
    
    private void subcommandStop(CommandSender sender)
    {
        getCore().stop();
        
        sendMsg(sender, _("stopCore.success"));
    }
    
    private void subcommandReload(Player player)
    {
        try
        {
            getCore().restart();
            
            if (player != null)
            {
                sendMsg(player, _("reloadPlugin.success"));
            }
        }
        catch (FatalReportedException ex)
        {
        }
    }
    
    private void subcommandBackupForce(CommandSender sender)
    {
        try
        {
            ReportedException.incrementRequestCount();
            
            File backupFile = getBackupManager().createBackup(false);
            
            if (sender instanceof Player)
            {
                sendMsg(sender, _("createBackup.success")
                        .replace("{0}", backupFile.getName()));
            }
        }
        catch (ReportedException ex)
        {
            if (sender instanceof Player)
            {
                sendMsg(sender, _("createBackup.fail"));
            }
        }
        finally
        {
            ReportedException.decrementRequestCount();
        }
    }
    
    private void subcommandBackupRestore(CommandSender sender)
    {
        File[] backups = getBackupManager().getBackups(true);
        
        if (backups.length == 0)
        {
            sendMsg(sender, _("restoreBackup.noBackups"));
            
            return;
        }
        
        String filename = backups[backups.length - 1].getName();
        
        subcommandBackupRestore_File(sender, filename);
    }
    
    private void subcommandBackupRestore_File(final CommandSender sender, final String filename)
    {
        assert filename != null;
        
        final File selectedBackup = getBackupManager().getBackupFile(filename);
        
        if (selectedBackup == null)
        {
            sendMsg(sender, _("restoreBackup.backupNotFound")
                    .replace("{0}", filename));
            
            return;
        }
        
        Date selectedBackupDate;
        
        try
        {
            selectedBackupDate = getBackupManager().parseBackupFilename(selectedBackup.getName());
        }
        catch (ParseException ex)
        {
            sendMsg(sender, _("restoreBackup.fail")
                    .replace("{0}", selectedBackup.getName()));
            
            return;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, _("restoreBackup.confirm.header"));
        sendMsg(sender, _("restoreBackup.confirm.typeToProceed"));
        sendMsg(sender, "");
        sendMsg(sender, _("restoreBackup.confirm.filename")
                .replace("{0}", selectedBackup.getName()));
        sendMsg(sender, _("restoreBackup.confirm.date")
                .replace("{0}", selectedBackupDate.toString()));
        sendMsg(sender, "");
        sendMsg(sender, _("restoreBackup.confirm.typeToCancel"));
        
        new ConfirmationWizard(sender, "proceed", new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ReportedException.incrementRequestCount();
                    
                    getBackupManager().restoreBackup(filename);
                    
                    if (sender instanceof Player)
                    {
                        sendMsg(sender, _("restoreBackup.success")
                                .replace("{0}", filename));
                    }
                }
                catch (FileNotFoundException | ReportedException ex)
                {
                    if (sender instanceof Player)
                    {
                        sendMsg(sender, _("restoreBackup.fail")
                                .replace("{0}", filename));
                    }
                }
                finally
                {
                    ReportedException.decrementRequestCount();
                }
            }
        }).createWizard();
    }
    
    private void subcommandBackupRestore_Time(CommandSender sender, String desiredDeltaTime)
    {
        assert desiredDeltaTime != null;
        
        File[] backups = getBackupManager().getBackups(true);
        
        long currentTimeMillis = System.currentTimeMillis();
        long desiredDeltaTimeMillis = TimeString.decode(desiredDeltaTime, TimeUnit.MILLISECONDS);
        long smallestDeltaDifference = Long.MAX_VALUE;
        
        File closestBackup = null;
        
        for (File backup : backups)
        {
            try
            {
                Date backupDate = getBackupManager().parseBackupFilename(backup.getName());
                long deltaTimeMillis = (currentTimeMillis - backupDate.getTime());
                long deltaDifference = Math.abs(desiredDeltaTimeMillis - deltaTimeMillis);
                
                if (closestBackup == null || deltaDifference < smallestDeltaDifference)
                {
                    closestBackup = backup;
                    smallestDeltaDifference = deltaDifference;
                }
            }
            catch (ParseException ex)
            {
                // If a ParseException has been thrown, the file is probably not a backup,
                // so we skip it without notice.
            }
        }
        
        if (closestBackup == null)
        {
            sendMsg(sender, _("restoreBackup.noBackups"));
            
            return;
        }
        
        subcommandBackupRestore_File(sender, closestBackup.getName());
    }
    
    private void subcommandBackupRemove(CommandSender sender, String amountString)
    {
        try
        {
            int amount = Integer.parseInt(amountString);
            
            if (amount > getConfig().getInt("backup.manual-remove-limit"))
            {
                amount = getConfig().getInt("backup.manual-remove-limit");
            }
            
            int effectiveAmount = getBackupManager().removeBackups(amount);
            
            if (sender instanceof Player)
            {
                sendMsg(sender, _("removeBackups.success")
                        .replace("{0}", String.valueOf(effectiveAmount)));
            }
        }
        catch (NumberFormatException ex)
        {
            sendMsg(sender, _("invalidParam")
                    .replace("{0}", "amount"));
        }
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
            sendMsg(player, _("globalpass.set.success"));
        }
    }
    
    private void subcommandGlobalpassRemove(Player player)
    {
        getCore().removeGlobalPassword();
        
        if (player != null)
        {
            sendMsg(player, _("globalpass.remove.success"));
        }
    }
    
    private void subcommandAccountStatus(CommandSender sender, String username)
    {
        StringBuilder status = new StringBuilder(); 
        
        if (getAccountManager().isRegistered(username))
        {
            status.append(_("accountStatus.registered"));
        }
        else
        {
            status.append(_("accountStatus.notRegistered"));
        }
        
        sendMsg(sender, _("accountStatus")
                .replace("{0}", username)
                .replace("{1}", status.toString()));
    }
    
    private void subcommandIpcount(CommandSender sender, String ip)
    {
        sendMsg(sender, _("ipcount")
            .replace("{0}", ip)
            .replace("{1}", String.valueOf(getAccountManager().countAccountsWithIp(ip))));
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
                        sendMsg(sender, _("invalidParam")
                                .replace("{0}", "value"));
                        
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
                        throw new Exception(_("onlyForPlayers"));
                    
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
                        sendMsg(sender, _("invalidParam")
                                .replace("{0}", "value"));
                        
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
                        throw new Exception(_("onlyForPlayers"));
                    
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
                sendMsg(sender, _("config.set.success")
                        .replace("{0}", args[2])
                        .replace("{1}", getConfig().toString(args[2])));
            }
            
            if (getConfig().getProperty(args[2]).requiresRestart())
            {
                sendMsg(sender, _("config.set.reloadPlugin"));
            }
        }
        catch (Exception ex)
        {
            sendMsg(sender, _("config.set.fail")
                    .replace("{0}", args[2])
                    .replace("{1}", ex.getMessage()));
        }
    }
    
    private void subcommandConfigGet(CommandSender sender, String path)
    {
        sendMsg(sender, _("config.get.property")
                .replace("{0}", path)
                .replace("{1}", getConfig().toString(path)));
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
                sendMsg(sender, _("invalidParam")
                        .replace("{0}", "page"));
                
                return;
            }
        }
        
        if (page <= 0)
        {
            page = 1;
        }
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, _("config.list.header1")
                .replace("{0}", String.valueOf(page))
                .replace("{1}", String.valueOf(pages)));
        sendMsg(sender, _("config.list.header2"));
        sendMsg(sender, _("config.list.header3"));
        
        for (Entry<String, Property> e : properties.entrySet())
        {
            if ((i > ((PROPERTIES_PER_PAGE * (page - 1)) - 1)) && (j < PROPERTIES_PER_PAGE))
            {
                sendMsg(sender, _("config.list.property")
                        .replace("{0}", e.getValue().getPath())
                        .replace("{1}", e.getValue().toString()));
                
                j++;
            }
            
            i++;
        }
        
        if (page > pages)
        {
            sendMsg(sender, _("config.list.noProperties"));
        }
    }
    
    private void subcommandConfigReload(CommandSender sender)
    {
        try
        {
            getConfig().load();
            
            log(Level.INFO, _("reloadConfig.success"));
            
            if (sender instanceof Player)
            {
                sendMsg(sender, _("reloadConfig.success"));
            }
        }
        catch (IOException | InvalidConfigurationException | InvalidPropertyValueException ex)
        {
            ex.printStackTrace();
            
            sendMsg(sender, _("reloadConfig.fail"));
        }
    }
    
    private void subcommandConvert(CommandSender sender)
    {
        new ConvertWizard(sender).createWizard();
    }
    
    private void subcommandStats(CommandSender sender)
    {
        int accountCount = getAccountManager().countAccounts();
        int uniqueIps = getAccountManager().countUniqueIps();
        int backupCount = getBackupManager().getBackups(false).length;
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
        
        sendMsg(sender, _("stats.header"));
        sendMsg(sender, _("stats.accountCount")
                .replace("{0}", String.valueOf(accountCount)));
        sendMsg(sender, _("stats.uniqueIps")
                .replace("{0}", String.valueOf(uniqueIps)));
        sendMsg(sender, _("stats.backupCount")
                .replace("{0}", String.valueOf(backupCount)));
        sendMsg(sender, "");
        sendMsg(sender, _("stats.logins")
                .replace("{0}", String.valueOf(getCore().getStats().getInt("logins"))));
        sendMsg(sender, _("stats.passwordChanges")
                .replace("{0}", String.valueOf(getCore().getStats().getInt("password-changes"))));
        
        if (sender instanceof Player)
        {
            sendMsg(sender, "");
        }
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
    
    private static String buildSubcommandHelp(String subcommand,
                                              String params,
                                              String descriptionLabel)
    {
        String line = _("subCmdHelpLine");
        
        if (params != null)
        {
            line = line.replace("{0}", "logit " + subcommand + " " + params);
        }
        else
        {
            line = line.replace("{0}", "logit " + subcommand);
        }
        
        return line.replace("{1}", _(descriptionLabel));
    }
}
