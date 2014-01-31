/*
 * LogItConfiguration.java
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
package io.github.lucaseasedup.logit.config;

import com.google.common.collect.ImmutableMap;
import io.github.lucaseasedup.logit.LogItPlugin;
import io.github.lucaseasedup.logit.util.IniUtils;
import io.github.lucaseasedup.logit.util.IoUtils;
import it.sauronsoftware.base64.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class LogItConfiguration extends PropertyObserver
{
    public void load() throws IOException, InvalidPropertyValueException
    {
        getPlugin().reloadConfig();
        getPlugin().getConfig().options().header(
             "# # # # # # # # # # # # # # #\n"
           + " LogIt Configuration File   #\n"
           + "# # # # # # # # # # # # # # #\n");
        
        File userDefFile = getDataFile(USER_CONFIG_DEF);
        
        if (!userDefFile.exists())
        {
            IoUtils.extractResource(PACKAGE_CONFIG_DEF, userDefFile);
        }
        
        String userDefBase64String;
        
        try (InputStream userDefInputStream = new FileInputStream(userDefFile))
        {
            userDefBase64String = IoUtils.toString(userDefInputStream);
        }
        
        Map<String, Map<String, String>> userDef =
                IniUtils.unserialize(decodeConfigDef(userDefBase64String));
        
        String jarUrlPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String jarPath = URLDecoder.decode(jarUrlPath, "UTF-8");
        
        try (ZipFile jarZipFile = new ZipFile(jarPath))
        {
            ZipEntry packageDefEntry = jarZipFile.getEntry(PACKAGE_CONFIG_DEF);
            
            try (InputStream packageDefInputStream = jarZipFile.getInputStream(packageDefEntry))
            {
                if (packageDefInputStream != null)
                {
                    String packageDefBase64String = IoUtils.toString(packageDefInputStream);
                    
                    if (!userDefBase64String.equals(packageDefBase64String))
                    {
                        Map<String, Map<String, String>> packageDef =
                                IniUtils.unserialize(decodeConfigDef(packageDefBase64String));
                        
                        try (OutputStream userDefOutputStream = new FileOutputStream(userDefFile))
                        {
                            updateConfigDef(userDef, packageDef, userDefOutputStream);
                        }
                    }
                }
            }
        }
        
        loadConfigDef(userDef);
        save();
        
        loaded = true;
    }
    
    public void save()
    {
        getPlugin().saveConfig();
    }
    
    public Map<String, Property> getProperties()
    {
        return ImmutableMap.copyOf(properties);
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
        return getPlugin().getConfig().getConfigurationSection(path);
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
    
    public Location getLocation(String path)
    {
        return (Location) properties.get(path).getValue();
    }
    
    public void set(String path, Object value) throws InvalidPropertyValueException
    {
        properties.get(path).set(value);
    }
    
    @Override
    public void update(Property p)
    {
        getPlugin().getConfig().set(p.getPath(), p.getValue());
        getPlugin().saveConfig();
        
        log(Level.INFO, LogItPlugin.getMessage("CONFIG_PROPERTY_SET_LOG", new String[]{
            "%path%", p.getPath(),
            "%value%", p.toString(),
        }));
    }
    
    public boolean isLoaded()
    {
        return loaded;
    }
    
    private void addProperty(String path,
                             PropertyType type,
                             boolean requiresRestart,
                             Object defaultValue,
                             PropertyValidator validator,
                             PropertyObserver obs) throws InvalidPropertyValueException
    {
        Object existingValue = getPlugin().getConfig().get(path, defaultValue);
        Property property = new Property(path, type, requiresRestart, existingValue, validator);
        
        if (obs != null)
        {
            property.addObserver(obs);
        }
        
        property.addObserver(this);
        
        Object value;
        
        if (!getPlugin().getConfig().isConfigurationSection(path))
        {
            value = property.getValue();
        }
        else
        {
            value = defaultValue;
        }
        
        if (validator != null && !validator.validate(path, type, value))
        {
            throw new InvalidPropertyValueException(path);
        }
        
        getPlugin().getConfig().set(property.getPath(), value);
        properties.put(property.getPath(), property);
    }
    
    private void updateConfigDef(Map<String, Map<String, String>> oldDef,
                                 Map<String, Map<String, String>> newDef,
                                 OutputStream os) throws IOException
    {
        Iterator<Entry<String, Map<String, String>>> it = oldDef.entrySet().iterator();
        
        while (it.hasNext())
        {
            Entry<String, Map<String, String>> entry = it.next();
            
            if (!newDef.containsKey(entry.getKey()))
            {
                getPlugin().getConfig().set(entry.getValue().get("path"), null);
                
                it.remove();
            }
        }
        
        for (Entry<String, Map<String, String>> entry : newDef.entrySet())
        {
            final Map<String, String> newDefSection = entry.getValue();
            final Map<String, String> oldDefSection = oldDef.get(entry.getKey());
            
            if (oldDefSection == null)
            {
                oldDef.put(entry.getKey(), new LinkedHashMap<String, String>()
                {{
                    put("path",             newDefSection.get("path"));
                    put("type",             newDefSection.get("type"));
                    put("requires_restart", newDefSection.get("requires_restart"));
                    put("default_value",    newDefSection.get("default_value"));
                    put("validator",        newDefSection.get("validator"));
                    put("observer",         newDefSection.get("observer"));
                }});
                
                continue;
            }
            
            if (!oldDefSection.get("path").equals(newDefSection.get("path")))
            {
                String oldPath = oldDefSection.get("path");
                Object oldValue = getPlugin().getConfig().get(oldPath);
                
                getPlugin().getConfig().set(oldPath, null);
                getPlugin().getConfig().set(newDefSection.get("path"), oldValue);
                
                oldDefSection.put("path", newDefSection.get("path"));
            }
            
            if (!oldDefSection.get("type").equals(newDefSection.get("type")))
            {
                oldDefSection.put("type", newDefSection.get("type"));
            }
            
            if (!oldDefSection.get("requires_restart").equals(newDefSection.get("requires_restart")))
            {
                oldDefSection.put("requires_restart", newDefSection.get("requires_restart"));
            }
            
            if (!oldDefSection.get("default_value").equals(newDefSection.get("default_value")))
            {
                oldDefSection.put("default_value", newDefSection.get("default_value"));
            }
            
            if (!oldDefSection.get("validator").equals(newDefSection.get("validator")))
            {
                oldDefSection.put("validator", newDefSection.get("validator"));
            }
            
            if (!oldDefSection.get("observer").equals(newDefSection.get("observer")))
            {
                oldDefSection.put("observer", newDefSection.get("observer"));
            }
        }
        
        os.write(encodeConfigDef(IniUtils.serialize(oldDef)).getBytes());
    }
    
    private void loadConfigDef(Map<String, Map<String, String>> def)
            throws InvalidPropertyValueException
    {
        for (Entry<String, Map<String, String>> entry : def.entrySet())
        {
            final Map<String, String> defSection = entry.getValue();
            
            String            path = defSection.get("path");
            PropertyType      type;
            boolean           requiresRestart = Boolean.valueOf(defSection.get("requires_restart"));
            Object            defaultValue;
            PropertyValidator validator = null;
            PropertyObserver  observer = null;
            
            String typeString = defSection.get("type");
            
            try
            {
                type = PropertyType.valueOf(typeString);
            }
            catch (IllegalArgumentException ex)
            {
                log(Level.WARNING, "Unknown property type: " + typeString);
                
                continue;
            }
            
            String defaultValueString = defSection.get("default_value");
            
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
            case LOCATION:
                defaultValue = new Location("world", 0, 0, 0, 0, 0); break;
            default:
                throw new RuntimeException("Unknown property type: " + type.toString());
            }
            
            String validatorClassName = defSection.get("validator");
            
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
                log(Level.WARNING, "Invalid property validator: " + validatorClassName + ".", ex);
                
                continue;
            }
            
            String observerClassName = defSection.get("observer");
            
            try
            {
                if (observerClassName != null && !observerClassName.isEmpty())
                {
                    @SuppressWarnings("unchecked")
                    Class<PropertyObserver> observerClass =
                            (Class<PropertyObserver>) Class.forName(observerClassName);
                    
                    observer = observerClass.getConstructor().newInstance();
                }
            }
            catch (ReflectiveOperationException ex)
            {
                getPlugin().getLogger().log(Level.WARNING,
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
    public static final String PACKAGE_CONFIG_DEF = "config-def.b64";
    
    private boolean loaded = false;
    private final Map<String, Property> properties = new LinkedHashMap<>();
}
