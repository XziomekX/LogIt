package io.github.lucaseasedup.logit.command.hub;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import java.util.List;
import io.github.lucaseasedup.logit.command.CommandAccess;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import io.github.lucaseasedup.logit.config.LocationSerializable;
import io.github.lucaseasedup.logit.config.PredefinedConfiguration;
import io.github.lucaseasedup.logit.config.Property;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class ConfigSetHubCommand extends HubCommand
{
    public ConfigSetHubCommand()
    {
        super("config set", new String[] {"path", "value"},
                new CommandAccess.Builder()
                        .permission("logit.config.set")
                        .playerOnly(false)
                        .runningCoreRequired(true)
                        .build(),
                new CommandHelpLine.Builder()
                        .command("logit config set")
                        .descriptionLabel("subCmdDesc.config.set")
                        .build());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        String hyphenatedPath = args[0];
        String camelCasePath = PredefinedConfiguration.getCamelCasePath(hyphenatedPath);
        Property property;
        
        if (!getConfig("config.yml").contains(hyphenatedPath))
        {
            if (!getConfig("config.yml").contains(camelCasePath))
            {
                sendMsg(sender, t("config.propertyNotFound")
                        .replace("{0}", hyphenatedPath));
                
                return;
            }
            else
            {
                property = getConfig("config.yml").getProperty(camelCasePath);
            }
        }
        else
        {
            property = getConfig("config.yml").getProperty(hyphenatedPath);
        }
        
        String inputValue = args[1];
        Object outputValue = null;
        
        try
        {
            switch (property.getType())
            {
            case CONFIGURATION_SECTION:
            case OBJECT:
                throw new RuntimeException("Unsupported property type conversion.");
                
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
                        throw new RuntimeException("Malformed color representation.");
                    
                    try
                    {
                        outputValue = Color.fromRGB(Integer.parseInt(rgb[0]),
                                                    Integer.parseInt(rgb[1]),
                                                    Integer.parseInt(rgb[2]));
                    }
                    catch (NumberFormatException ex)
                    {
                        sendMsg(sender, t("invalidParam")
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
                throw new RuntimeException("Unsupported property type conversion.");
                
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
                        throw new RuntimeException(t("onlyForPlayers"));
                    
                    Player player = ((Player) sender);
                    
                    outputValue = new Vector(player.getLocation().getX(),
                                             player.getLocation().getY(),
                                             player.getLocation().getZ());
                }
                else
                {
                    String[] axes = inputValue.split(" ");
                    
                    if (axes.length != 3)
                        throw new RuntimeException("Malformed vector representation.");
                    
                    try
                    {
                        outputValue = new Vector(Double.parseDouble(axes[0]),
                                                 Double.parseDouble(axes[1]),
                                                 Double.parseDouble(axes[2]));
                    }
                    catch (NumberFormatException ex)
                    {
                        sendMsg(sender, t("invalidParam")
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
                throw new RuntimeException("Unsupported property type conversion.");
                
            case LOCATION:
            {
                if ("$".equals(inputValue))
                {
                    if (!(sender instanceof Player))
                        throw new RuntimeException(t("onlyForPlayers"));
                    
                    Location loc = ((Player) sender).getLocation();
                    
                    outputValue = new LocationSerializable(loc.getWorld().getName(),
                            loc.getX(), loc.getY(), loc.getZ(),
                            loc.getYaw(), loc.getPitch());
                }
                else
                {
                    throw new RuntimeException("Unsupported property type conversion.");
                }
                
                break;
            }
            
            default:
                throw new RuntimeException("Unknown property type.");
            }
            
            property.set(outputValue);
            
            if (sender instanceof Player)
            {
                sendMsg(sender, t("config.set.success")
                        .replace("{0}", property.getPath())
                        .replace("{1}", property.getStringifiedValue()));
            }
            
            if (property.requiresRestart())
            {
                sendMsg(sender, t("config.set.reloadPlugin"));
            }
        }
        catch (RuntimeException ex)
        {
            String exMsg = ex.getMessage();
            
            if (exMsg == null)
            {
                exMsg = ex.getClass().getSimpleName();
                
                log(Level.WARNING, ex);
            }
            
            sendMsg(sender, t("config.set.fail")
                    .replace("{0}", property.getPath())
                    .replace("{1}", exMsg));
        }
    }
    
    @Override
    public List<String> complete(CommandSender sender, String[] args)
    {
        if (!getConfig("secret.yml").getBoolean("tabCompletion"))
            return null;
        
        if (args.length == 1)
        {
            return getTabCompleter().completeConfigProperty(args[0]);
        }
        
        return null;
    }
}
