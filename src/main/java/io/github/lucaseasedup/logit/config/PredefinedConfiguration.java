/*
 * PredefinedConfiguration.java
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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import com.google.common.collect.ImmutableMap;
import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.TimeString;
import io.github.lucaseasedup.logit.TimeUnit;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.Color;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class PredefinedConfiguration
        extends PropertyObserver
        implements PropertyHolder, Disposable
{
    public PredefinedConfiguration(String filename,
                                   String userConfigDef,
                                   String packageConfigDef,
                                   String header)
    {
        this.file = getDataFile(filename);
        this.configuration = YamlConfiguration.loadConfiguration(file);
        this.userConfigDef = userConfigDef;
        this.packageConfigDef = packageConfigDef;
        this.header = header;
    }
    
    @Override
    public void dispose()
    {
        if (properties != null)
        {
            for (Property property : properties.values())
            {
                property.dispose();
            }
            
            properties.clear();
            properties = null;
        }
    }
    
    public void load() throws IOException,
                              InvalidConfigurationException,
                              InvalidPropertyValueException
    {
        if (!file.exists())
        {
            file.createNewFile();
        }
        
        configuration.load(file);
        
        if (header != null)
        {
            configuration.options().header(header);
        }
        
        File userDefFile = getDataFile(userConfigDef);
        
        if (!userDefFile.exists())
        {
            IoUtils.extractResource(packageConfigDef, userDefFile);
        }
        
        String userDefBase64String;
        
        try (InputStream userDefInputStream = new FileInputStream(userDefFile))
        {
            userDefBase64String = IoUtils.toString(userDefInputStream);
        }
        
        Map<String, Map<String, String>> userDef =
                IniUtils.unserialize(decodeConfigDef(userDefBase64String));
        
        String jarUrlPath =
                getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String jarPath = URLDecoder.decode(jarUrlPath, "UTF-8");
        
        try (ZipFile jarZipFile = new ZipFile(jarPath))
        {
            ZipEntry packageDefEntry = jarZipFile.getEntry(packageConfigDef);
            
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
    
    /**
     * Saves the config to a predefined file.
     * 
     * @throws IOException if an I/O error occurred.
     */
    public void save() throws IOException
    {
        configuration.save(file);
    }
    
    @Override
    public Map<String, Property> getProperties()
    {
        return ImmutableMap.copyOf(properties);
    }
    
    /**
     * Returns a property object at the given path.
     * 
     * @param path the path.
     * 
     * @return the property object.
     */
    @Override
    public Property getProperty(String path)
    {
        return properties.get(path);
    }
    
    /**
     * Checks if the config contains a property at the given path.
     * 
     * @param path the path.
     * 
     * @return {@code true} if such property exists; {@code false} otherwise.
     */
    @Override
    public boolean contains(String path)
    {
        return properties.containsKey(path);
    }
    
    @Override
    public Set<String> getKeys(String path)
    {
        return configuration.getConfigurationSection(path).getKeys(false);
    }
    
    @Override
    public Map<String, Object> getValues(String path)
    {
        Map<String, Object> values = new LinkedHashMap<>();
        
        for (String key : getKeys(path))
        {
            values.put(key, getString(path + "." + key));
        }
        
        return values;
    }
    
    @Override
    public Object get(String path)
    {
        return properties.get(path);
    }
    
    @Override
    public boolean getBoolean(String path)
    {
        return (Boolean) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Boolean> getBooleanList(String path)
    {
        return (List<Boolean>) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Byte> getByteList(String path)
    {
        return (List<Byte>) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Character> getCharacterList(String path)
    {
        return (List<Character>) properties.get(path).getValue();
    }
    
    @Override
    public Color getColor(String path)
    {
        return (Color) properties.get(path).getValue();
    }
    
    @Override
    public double getDouble(String path)
    {
        return (Double) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Double> getDoubleList(String path)
    {
        return (List<Double>) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Float> getFloatList(String path)
    {
        return (List<Float>) properties.get(path).getValue();
    }
    
    @Override
    public int getInt(String path)
    {
        return (Integer) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> getIntegerList(String path)
    {
        return (List<Integer>) properties.get(path).getValue();
    }
    
    @Override
    public ItemStack getItemStack(String path)
    {
        return (ItemStack) properties.get(path).getValue();
    }
    
    @Override
    public List<?> getList(String path)
    {
        return (List<?>) properties.get(path).getValue();
    }
    
    @Override
    public long getLong(String path)
    {
        return (Long) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getLongList(String path)
    {
        return (List<Long>) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<?, ?>> getMapList(String path)
    {
        return (List<Map<?, ?>>) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Short> getShortList(String path)
    {
        return (List<Short>) properties.get(path).getValue();
    }
    
    @Override
    public String getString(String path)
    {
        return (String) properties.get(path).getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path)
    {
        return (List<String>) properties.get(path).getValue();
    }
    
    @Override
    public Vector getVector(String path)
    {
        return (Vector) properties.get(path).getValue();
    }
    
    @Override
    public LocationSerializable getLocation(String path)
    {
        return (LocationSerializable) properties.get(path).getValue();
    }
    
    @Override
    public long getTime(String path, TimeUnit convertTo)
    {
        if (path == null || convertTo == null)
            throw new IllegalArgumentException();
        
        return TimeString.decode(getString(path), convertTo);
    }
    
    /**
     * Sets a new value for a property at the given path.
     * 
     * @param path  the property path.
     * @param value the new value.
     * 
     * @throws InvalidPropertyValueException if the value provided
     *                                       is not valid for this property.
     */
    @Override
    public void set(String path, Object value) throws InvalidPropertyValueException
    {
        properties.get(path).set(value);
    }
    
    /**
     * Internal method. Do not call directly.
     */
    @Override
    public void update(Property p)
    {
        configuration.set(p.getPath(), p.getValue());
        
        try
        {
            save();
            
            log(Level.INFO, _("config.set.success.log")
                    .replace("{0}", p.getPath())
                    .replace("{1}", p.toString()));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, _("config.set.fail.log"), ex);
        }
    }
    
    /**
     * Checks if the config has been successfully loaded.
     * 
     * @return {@code true} if the config has been loaded; {@code false} otherwise.
     */
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
        Object existingValue = configuration.get(path, defaultValue);
        Property property = new Property(path, type, requiresRestart, existingValue, validator);
        
        if (obs != null)
        {
            property.addObserver(obs);
        }
        
        property.addObserver(this);
        
        Object value;
        
        if (!configuration.isConfigurationSection(path))
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
        
        configuration.set(property.getPath(), value);
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
                configuration.set(entry.getValue().get("path"), null);
                
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
                Object oldValue = configuration.get(oldPath);
                
                configuration.set(oldPath, null);
                configuration.set(newDefSection.get("path"), oldValue);
                
                oldDefSection.put("path", newDefSection.get("path"));
            }
            
            if (!compareSectionKeys(oldDefSection, newDefSection, "type"))
            {
                oldDefSection.put("type", newDefSection.get("type"));
            }
            
            if (!compareSectionKeys(oldDefSection, newDefSection, "requires_restart"))
            {
                oldDefSection.put("requires_restart", newDefSection.get("requires_restart"));
            }
            
            if (!compareSectionKeys(oldDefSection, newDefSection, "default_value"))
            {
                oldDefSection.put("default_value", newDefSection.get("default_value"));
            }
            
            if (!compareSectionKeys(oldDefSection, newDefSection, "validator"))
            {
                oldDefSection.put("validator", newDefSection.get("validator"));
            }
            
            if (!compareSectionKeys(oldDefSection, newDefSection, "observer"))
            {
                oldDefSection.put("observer", newDefSection.get("observer"));
            }
        }
        
        os.write(encodeConfigDef(IniUtils.serialize(oldDef)).getBytes());
    }
    
    private boolean compareSectionKeys(Map<String, String> oldDefSection,
                                       Map<String, String> newDefSection,
                                       String key)
    {
        return oldDefSection.get(key).equals(newDefSection.get(key));
    }
    
    private void loadConfigDef(Map<String, Map<String, String>> def)
            throws InvalidPropertyValueException
    {
        for (Entry<String, Map<String, String>> entry : def.entrySet())
        {
            final Map<String, String> defSection = entry.getValue();
            
            String path = defSection.get("path");
            PropertyType type;
            boolean requiresRestart = Boolean.valueOf(defSection.get("requires_restart"));
            Object defaultValue;
            PropertyValidator validator = null;
            PropertyObserver observer = null;
            
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
            case CONFIGURATION_SECTION:
            {
                defaultValue = configuration.getConfigurationSection(path);
                
                if (defaultValue == null)
                {
                    defaultValue = configuration.createSection(path);
                }
                
                break;
            }
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
                            defaultValue = Color.fromRGB(Integer.parseInt(rgb[0]),
                                                         Integer.parseInt(rgb[1]),
                                                         Integer.parseInt(rgb[2]));
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
                defaultValue = new LocationSerializable("world", 0, 0, 0, 0, 0); break;
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
                log(Level.WARNING, "Invalid property observer: " + observerClassName + ".", ex);
                
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
    
    private final File file;
    private final FileConfiguration configuration;
    private final String userConfigDef;
    private final String packageConfigDef;
    private final String header;
    private boolean loaded = false;
    private Map<String, Property> properties = new LinkedHashMap<>();
}
