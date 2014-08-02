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

import static io.github.lucaseasedup.logit.util.MessageHelper.t;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class PredefinedConfiguration extends PropertyObserver implements PropertyHolder
{
    public PredefinedConfiguration(String filename,
                                   String userDefPathname,
                                   String packageDefPathname,
                                   String header)
    {
        if (StringUtils.isBlank(filename)
                || userDefPathname == null || packageDefPathname == null)
        {
            throw new IllegalArgumentException();
        }
        
        this.file = getDataFile(filename);
        this.userDefPathname = userDefPathname;
        this.packageDefPathname = packageDefPathname;
        this.header = header;
    }
    
    @Override
    public void dispose()
    {
        configuration = null;
        
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
    
    public void load() throws IOException, InvalidPropertyValueException
    {
        reopen();
        
        File backupFile = new File(file.getCanonicalPath() + ".bak");
        Files.copy(file, backupFile);
        
        String packageDefString = readPackageDefString();
        String userDefString;
        
        Map<String, Map<String, String>> packageDef = IniUtils.unserialize(packageDefString);
        Map<String, Map<String, String>> userDef;
        
        File userDefFile = getDataFile(userDefPathname);
        boolean userDefChanged = false;
        
        if (!userDefFile.exists())
        {
            userDefFile.getParentFile().mkdirs();
            userDefString = packageDefString;
            userDef = IoUtils.deepCopy(packageDef);
            
            for (Map<String, String> userDefSection : userDef.values())
            {
                registerProperty(userDefSection);
            }
            
            userDefChanged = true;
        }
        else
        {
            userDefString = readUserDefString();
            userDef = IniUtils.unserialize(userDefString);
            
            // Backup all user-specified values
            YamlConfiguration backup = YamlConfiguration.loadConfiguration(file);
            
            // Clear the config
            for (String path : configuration.getKeys(true))
            {
                removePath(configuration, path);
            }
            
            /* Remove all unused sections from the user def */
            Iterator<Map.Entry<String, Map<String, String>>> it =
                    userDef.entrySet().iterator();
            
            while (it.hasNext())
            {
                Map.Entry<String, Map<String, String>> section = it.next();
                
                if (!packageDef.containsKey(section.getKey()))
                {
                    removePath(backup, section.getValue().get("path"));
                    
                    it.remove();
                    
                    userDefChanged = true;
                }
            }
            
            /* Iterate through the package def to update the user def and config file */
            for (Map.Entry<String, Map<String, String>> section : packageDef.entrySet())
            {
                String guid = section.getKey();
                Map<String, String> packageDefSection = section.getValue();
                Map<String, String> userDefSection = userDef.get(guid);
                
                String newPath = packageDefSection.get("path");
                String oldPath;
                
                if (userDefSection == null)
                {
                    oldPath = newPath;
                    
                    userDefSection = new LinkedHashMap<>(packageDefSection);
                    userDef.put(guid, userDefSection);
                    userDefChanged = true;
                }
                else
                {
                    oldPath = userDefSection.get("path");
                    
                    for (Map.Entry<String, String> property : packageDefSection.entrySet())
                    {
                        String key = property.getKey();
                        
                        if (!property.getValue().equals(userDefSection.get(key)))
                        {
                            userDefSection.put(key, property.getValue());
                            userDefChanged = true;
                        }
                    }
                }
                
                registerProperty(userDefSection);
                
                /* Restore user-specified value */
                if (backup.contains(oldPath))
                {
                    Object backupValue = backup.get(oldPath);
                    
                    configuration.set(newPath, backupValue);
                    properties.get(newPath).setSilently(backupValue);
                    
                    removePath(backup, oldPath);
                }
            }
            
            userDefString = IniUtils.serialize(userDef);
        }
        
        if (userDefChanged)
        {
            try (OutputStream userDefOutputStream = new FileOutputStream(userDefFile))
            {
                userDefOutputStream.write(encodeUserDef(userDefString).getBytes());
            }
        }
        
        save();
        backupFile.delete();
        
        loaded = true;
    }
    
    private String readPackageDefString() throws IOException
    {
        String jarUrlPath =
                getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String jarPath = URLDecoder.decode(jarUrlPath, "UTF-8");
        
        try (ZipFile jarZipFile = new ZipFile(jarPath))
        {
            ZipEntry packageDefEntry = jarZipFile.getEntry(packageDefPathname);
            
            try (InputStream packageDefInputStream = jarZipFile.getInputStream(packageDefEntry))
            {
                return IoUtils.toString(packageDefInputStream);
            }
        }
    }
    
    private String readUserDefString() throws IOException
    {
        try (InputStream userDefInputStream = new FileInputStream(getDataFile(userDefPathname)))
        {
            return decodeConfigDef(IoUtils.toString(userDefInputStream));
        }
    }
    
    private void reopen() throws IOException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        
        configuration = YamlConfiguration.loadConfiguration(file);
        
        if (header != null)
        {
            configuration.options().header(header);
        }
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
            
            log(Level.INFO, t("config.set.success.log")
                    .replace("{0}", p.getPath())
                    .replace("{1}", p.toString()));
        }
        catch (IOException ex)
        {
            log(Level.WARNING, t("config.set.fail.log"), ex);
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
    
    private void removePath(ConfigurationSection section, String path)
    {
        section.set(path, null);
        
        if (!path.contains("."))
        {
            return;
        }
        
        String parentPath = path.substring(0, path.lastIndexOf('.'));
        
        if (properties.containsKey(parentPath))
        {
            return;
        }
        
        ConfigurationSection parentSection = section.getConfigurationSection(parentPath);
        
        if (parentSection == null || !parentSection.getKeys(false).isEmpty())
        {
            return;
        }
        
        removePath(section, parentPath);
    }
    
    private void registerProperty(String path,
                                  PropertyType type,
                                  boolean requiresRestart,
                                  Object defaultValue,
                                  PropertyValidator validator,
                                  PropertyObserver obs)
            throws InvalidPropertyValueException
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
    
    private void registerProperty(Map<String, String> defSection)
            throws InvalidPropertyValueException
    {
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
            
            return;
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
        case OBJECT:
            defaultValue = null;
            break;
            
        case BOOLEAN:
            defaultValue = Boolean.valueOf(defaultValueString);
            break;
            
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
        case DOUBLE:
            defaultValue = Double.valueOf(defaultValueString);
            break;
            
        case INT:
            defaultValue = Integer.valueOf(defaultValueString);
            break;
            
        case ITEM_STACK:
            defaultValue = null;
            break;
            
        case LONG:
            defaultValue = Long.valueOf(defaultValueString);
            break;
            
        case STRING:
            defaultValue = defaultValueString;
            break;
            
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
            defaultValue = new ArrayList<>(0);
            break;
            
        case LOCATION:
            defaultValue = new LocationSerializable("world", 0, 0, 0, 0, 0);
            break;
            
        default:
            throw new RuntimeException("Unknown property type: " + type.toString());
        }
        
        String validatorClassName = defSection.get("validator");
        
        if (!StringUtils.isBlank(validatorClassName))
        {
            try
            {
                @SuppressWarnings("unchecked")
                Class<PropertyValidator> validatorClass =
                        (Class<PropertyValidator>) Class.forName(validatorClassName);
                
                validator = validatorClass.getConstructor().newInstance();
            }
            catch (ReflectiveOperationException ex)
            {
                log(Level.WARNING, "Invalid property validator: " + validatorClassName + ".", ex);
                
                return;
            }
        }
        
        String observerClassName = defSection.get("observer");
        
        if (!StringUtils.isBlank(observerClassName))
        {
            try
            {
                @SuppressWarnings("unchecked")
                Class<PropertyObserver> observerClass =
                        (Class<PropertyObserver>) Class.forName(observerClassName);
                
                observer = observerClass.getConstructor().newInstance();
            }
            catch (ReflectiveOperationException ex)
            {
                log(Level.WARNING, "Invalid property observer: " + observerClassName + ".", ex);
                
                return;
            }
        }
        
        registerProperty(path, type, requiresRestart, defaultValue, validator, observer);
    }
    
    private String encodeUserDef(String input)
    {
        return Base64.encode(input);
    }
    
    private String decodeConfigDef(String input)
    {
        return Base64.decode(input);
    }
    
    /**
     * Converts a hyphenated path (example-path.secret-setting)
     * to a camelCase path (examplePath.secretSetting).
     * 
     * @param hyphenatedPath the hyphenated path.
     * 
     * @return the camelCase equivalent of the provided hyphenated path.
     */
    public static String getCamelCasePath(String hyphenatedPath)
    {
        Matcher matcher = HYPHENATED_PATH_PATTERN.matcher(hyphenatedPath);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        
        while (matcher.find())
        {
            sb.append(hyphenatedPath.substring(last, matcher.start()));
            sb.append(matcher.group(1).toUpperCase());
            
            last = matcher.end();
        }
        
        sb.append(hyphenatedPath.substring(last));
        
        return sb.toString();
    }
    
    private static final Pattern HYPHENATED_PATH_PATTERN = Pattern.compile("\\-([a-z])");
    
    private final File file;
    private FileConfiguration configuration;
    private final String userDefPathname;
    private final String packageDefPathname;
    private final String header;
    private boolean loaded = false;
    private Map<String, Property> properties = new LinkedHashMap<>();
}
