/*
 * LogItConfiguration.java
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
package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.IniFile;
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItPlugin;
import io.github.lucaseasedup.logit.util.FileUtils;
import it.sauronsoftware.base64.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author LucasEasedUp
 */
public final class LogItConfiguration extends PropertyObserver
{
    public LogItConfiguration(LogItPlugin plugin)
    {
        super(plugin.getCore());
        
        this.plugin = plugin;
    }
    
    public void load() throws IOException
    {
        plugin.reloadConfig();
        plugin.getConfig().options().header(
             "# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n"
           + "Visit http://dev.bukkit.org/bukkit-plugins/logit/pages/configuration-v0-4-8-5/ for help in configuring LogIt. #\n"
           + "# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n");
        
        File userDefFile = new File(plugin.getDataFolder(), USER_CONFIG_DEF);
        
        if (!userDefFile.exists())
        {
            FileUtils.extractResource(PACKAGE_CONFIG_DEF, userDefFile);
        }
        
        String userDefBase64String = IOUtils.toString(new FileInputStream(userDefFile));
        IniFile userDef = new IniFile(decodeConfigDef(userDefBase64String));
        
        InputStream packageDefInputStream = LogItConfiguration.class.getResourceAsStream(PACKAGE_CONFIG_DEF);
        
        if (packageDefInputStream != null)
        {
            String packageDefBase64String = IOUtils.toString(packageDefInputStream);
            
            if (!userDefBase64String.equals(packageDefBase64String))
            {
                IniFile packageDef = new IniFile(decodeConfigDef(packageDefBase64String));
                
                updateConfigDef(userDef, packageDef, new FileOutputStream(userDefFile));
            }
        }
        
        loadConfigDef(userDef);
        save();
    }
    
    public void save()
    {
        plugin.saveConfig();
    }
    
    public Map<String, Property> getProperties()
    {
        return properties;
    }
    
    public Property getProperty(String path)
    {
        return properties.get(path);
    }
    
    public boolean contains(String path)
    {
        return properties.containsKey(path);
    }
    
    public PropertyType getType(String path)
    {
        return properties.get(path).getType();
    }
    
    public String toString(String path)
    {
        return properties.get(path).toString();
    }
    
    public ConfigurationSection getConfigurationSection(String path)
    {
        return plugin.getConfig().getConfigurationSection(path);
    }
    
    public Object get(String path)
    {
        return properties.get(path);
    }
    
    public boolean getBoolean(String path)
    {
        return (Boolean) properties.get(path).getValue();
    }
    
    public Color getColor(String path)
    {
        return (Color) properties.get(path).getValue();
    }
    
    public double getDouble(String path)
    {
        return (Double) properties.get(path).getValue();
    }
    
    public int getInt(String path)
    {
        return (Integer) properties.get(path).getValue();
    }
    
    public ItemStack getItemStack(String path)
    {
        return (ItemStack) properties.get(path).getValue();
    }
    
    public long getLong(String path)
    {
        return (Long) properties.get(path).getValue();
    }
    
    public String getString(String path)
    {
        return (String) properties.get(path).getValue();
    }
    
    public Vector getVector(String path)
    {
        return (Vector) properties.get(path).getValue();
    }
    
    @SuppressWarnings("rawtypes")
    public List getList(String path)
    {
        return (List) properties.get(path).getValue();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path)
    {
        return (List<String>) properties.get(path).getValue();
    }
    
    public void set(String path, Object value) throws InvalidPropertyValueException
    {
        properties.get(path).set(value);
    }
    
    @Override
    public void update(Property p)
    {
        plugin.getConfig().set(p.getPath(), p.getValue());
        plugin.saveConfig();
        
        plugin.getLogger().log(Level.INFO, LogItPlugin.getMessage("CONFIG_PROPERTY_SET_LOG", new String[]{
            "%path%", p.getPath(),
            "%value%", p.toString(),
        }));
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean requiresRestart,
                               Object defaultValue,
                               PropertyValidator validator,
                               PropertyObserver obs)
    {
        Property property =
                new Property(path, type, requiresRestart, plugin.getConfig().get(path, defaultValue), validator);
        
        if (obs != null)
        {
            property.addObserver(obs);
        }
        
        property.addObserver(this);
        
        plugin.getConfig().set(property.getPath(), property.getValue());
        properties.put(property.getPath(), property);
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean requiresRestart,
                               Object defaultValue,
                               PropertyObserver obs)
    {
        addProperty(path, type, requiresRestart, defaultValue, null, obs);
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean requiresRestart,
                               Object defaultValue,
                               PropertyValidator validator)
    {
        addProperty(path, type, requiresRestart, defaultValue, validator, null);
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean requiresRestart,
                               Object defaultValue)
    {
        addProperty(path, type, requiresRestart, defaultValue, null, null);
    }
    
    private void updateConfigDef(IniFile oldDef, IniFile newDef, OutputStream os) throws IOException
    {
        for (String uuid : newDef.getSections())
        {
            if (!oldDef.hasSection(uuid))
            {
                oldDef.putSection(uuid);
                oldDef.putString(uuid, "path", newDef.getString(uuid, "path"));
                oldDef.putString(uuid, "type", newDef.getString(uuid, "type"));
                oldDef.putString(uuid, "requires_restart", newDef.getString(uuid, "requires_restart"));
                oldDef.putString(uuid, "default_value", newDef.getString(uuid, "default_value"));
                oldDef.putString(uuid, "validator", newDef.getString(uuid, "validator"));
                oldDef.putString(uuid, "observer", newDef.getString(uuid, "observer"));
            }
            else
            {
                if (!oldDef.getString(uuid, "path").equals(newDef.getString(uuid, "path")))
                {
                    Object val = plugin.getConfig().get(oldDef.getString(uuid, "path"));
                    
                    plugin.getConfig().set(oldDef.getString(uuid, "path"), null);
                    plugin.getConfig().set(newDef.getString(uuid, "path"), val);
                    
                    oldDef.putString(uuid, "path", newDef.getString(uuid, "path"));
                }
                
                if (!oldDef.getString(uuid, "type").equals(newDef.getString(uuid, "type")))
                {
                    oldDef.putString(uuid, "type", newDef.getString(uuid, "type"));
                }
                
                if (!oldDef.getString(uuid, "requires_restart").equals(newDef.getString(uuid, "requires_restart")))
                {
                    oldDef.putString(uuid, "requires_restart", newDef.getString(uuid, "requires_restart"));
                }
                
                if (!oldDef.getString(uuid, "default_value").equals(newDef.getString(uuid, "default_value")))
                {
                    oldDef.putString(uuid, "default_value", newDef.getString(uuid, "default_value"));
                }
                
                if (!oldDef.getString(uuid, "validator").equals(newDef.getString(uuid, "validator")))
                {
                    oldDef.putString(uuid, "validator", newDef.getString(uuid, "validator"));
                }
                
                if (!oldDef.getString(uuid, "observer").equals(newDef.getString(uuid, "observer")))
                {
                    oldDef.putString(uuid, "observer", newDef.getString(uuid, "observer"));
                }
            }
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        oldDef.save(baos);
        
        try (FileOutputStream fos = new FileOutputStream(new File(plugin.getDataFolder(), USER_CONFIG_DEF)))
        {
            fos.write(encodeConfigDef(baos.toString()).getBytes());
        }
    }
    
    private void loadConfigDef(IniFile def)
    {
        Set<String> properties = def.getSections();
        
        for (String uuid : properties)
        {
            String path = def.getString(uuid, "path");
            PropertyType type = null;
            boolean requiresRestart = def.getBoolean(uuid, "requires_restart", true);
            Object defaultValue = null;
            PropertyValidator validator = null;
            PropertyObserver observer = null;
            
            String typeString = def.getString(uuid, "type");
            
            try
            {
                type = PropertyType.valueOf(typeString);
            }
            catch (IllegalArgumentException ex)
            {
                plugin.getLogger().log(Level.WARNING, "Unknown property type: " + typeString);
                
                continue;
            }
            
            String defaultValueString = def.getString(uuid, "default_value");
            
            switch (type)
            {
            case OBJECT:  defaultValue = null;                                break;
            case BOOLEAN: defaultValue = Boolean.valueOf(defaultValueString); break;
            case COLOR:
            {
                switch (defaultValueString.toLowerCase())
                {
                case "aqua":    defaultValue = Color.AQUA;    break;
                case "black":   defaultValue = Color.BLACK;   break;
                case "blue":    defaultValue = Color.BLUE;    break;
                case "fuchsia": defaultValue = Color.FUCHSIA; break;
                case "gray":    defaultValue = Color.GRAY;    break;
                case "green":   defaultValue = Color.GREEN;   break;
                case "lime":    defaultValue = Color.LIME;    break;
                case "maroon":  defaultValue = Color.MAROON;  break;
                case "navy":    defaultValue = Color.NAVY;    break;
                case "olive":   defaultValue = Color.OLIVE;   break;
                case "orange":  defaultValue = Color.ORANGE;  break;
                case "purple":  defaultValue = Color.PURPLE;  break;
                case "red":     defaultValue = Color.RED;     break;
                case "silver":  defaultValue = Color.SILVER;  break;
                case "teal":    defaultValue = Color.TEAL;    break;
                case "white":   defaultValue = Color.WHITE;   break;
                case "yellow":  defaultValue = Color.YELLOW;  break;
                default:
                    {
                        String[] rgb = defaultValueString.split(" ");
                        
                        if (rgb.length == 3)
                        {
                            defaultValue = Color.fromRGB(Integer.valueOf(rgb[0]),
                                                         Integer.valueOf(rgb[1]),
                                                         Integer.valueOf(rgb[2]));
                        }
                        else
                        {
                            defaultValue = Color.BLACK;
                        }
                        
                        break;
                    }
                }
                
                break;
            }
            case DOUBLE:     defaultValue = Double.valueOf(defaultValueString);  break;
            case INT:        defaultValue = Integer.valueOf(defaultValueString); break;
            case ITEM_STACK: defaultValue = null;                                break;
            case LONG:       defaultValue = Long.valueOf(defaultValueString);    break;
            case STRING:     defaultValue = defaultValueString;                  break;
            case VECTOR:
            {
                String[] axes = defaultValueString.split(" ");
                
                if (axes.length == 3)
                {
                    defaultValue = new Vector(Double.valueOf(axes[0]),
                                              Double.valueOf(axes[1]),
                                              Double.valueOf(axes[2]));
                }
                else
                {
                    defaultValue = new Vector(0, 0, 0);
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
                defaultValue = new ArrayList<>(0); break;
            default:
                throw new RuntimeException("Unknown property type.");
            }
            
            String validatorClassName = def.getString(uuid, "validator");
            
            try
            {
                if (validatorClassName != null && !validatorClassName.isEmpty())
                {
                    @SuppressWarnings("unchecked")
                    Class<PropertyValidator> validatorClass =
                            (Class<PropertyValidator>) Class.forName(validatorClassName);
                    
                    validator = validatorClass.getConstructor().newInstance();
                }

            }
            catch (ReflectiveOperationException ex)
            {
                plugin.getLogger().log(Level.WARNING,
                        "Invalid property validator: " + validatorClassName + ".", ex);
                
                continue;
            }
            
            String observerClassName = def.getString(uuid, "observer");
            
            try
            {
                if (observerClassName != null && !observerClassName.isEmpty())
                {
                    @SuppressWarnings("unchecked")
                    Class<PropertyObserver> observerClass =
                            (Class<PropertyObserver>) Class.forName(observerClassName);
                    
                    observer = observerClass.getConstructor(LogItCore.class).newInstance(plugin.getCore());
                }
            }
            catch (ReflectiveOperationException ex)
            {
                plugin.getLogger().log(Level.WARNING,
                        "Invalid property observer: " + observerClassName + ".", ex);
                
                continue;
            }
            
            addProperty(path, type, requiresRestart, defaultValue, validator, observer);
        }
    }
    
    private String encodeConfigDef(String input)
    {
        return Base64.encode(input);
    }
    
    private String decodeConfigDef(String input)
    {
        return Base64.decode(input);
    }
    
    public static final String USER_CONFIG_DEF = "config-def.b64";
    public static final String PACKAGE_CONFIG_DEF = "/config-def.b64";
    
    private final LogItPlugin plugin;
    private final Map<String, Property> properties = new LinkedHashMap<>();
}
