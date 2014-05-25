/*
 * ConfigSetHubCommand.java
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
package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.config.LocationSerializable;
import io.github.lucaseasedup.logit.config.PropertyType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class ConfigSetHubCommand extends HubCommand
{
    public ConfigSetHubCommand()
    {
        super("config set", new String[] {"path", "value"}, "logit.config.set", false, true,
                new CommandHelpLine.Builder()
                        .command("logit config set")
                        .descriptionLabel("subCmdDesc.config.set")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!getConfig().contains(args[0]))
        {
            sendMsg(sender, _("config.propertyNotFound")
                    .replace("{0}", args[0]));
            
            return;
        }
        
        PropertyType type = getConfig().getType(args[0]);
        String inputValue = args[1];
        Object outputValue = null;
        
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
            
            getConfig().set(args[0], outputValue);
            
            if (sender instanceof Player)
            {
                sendMsg(sender, _("config.set.success")
                        .replace("{0}", args[0])
                        .replace("{1}", getConfig().toString(args[0])));
            }
            
            if (getConfig().getProperty(args[0]).requiresRestart())
            {
                sendMsg(sender, _("config.set.reloadPlugin"));
            }
        }
        catch (Exception ex)
        {
            sendMsg(sender, _("config.set.fail")
                    .replace("{0}", args[0])
                    .replace("{1}", ex.getMessage()));
        }
    }
}
