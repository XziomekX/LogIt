/*
 * ProfileManager.java
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
package io.github.lucaseasedup.logit.profile;

import com.google.common.collect.ImmutableList;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.profile.field.Field;
import io.github.lucaseasedup.logit.profile.field.FloatField;
import io.github.lucaseasedup.logit.profile.field.IntegerField;
import io.github.lucaseasedup.logit.profile.field.SetField;
import io.github.lucaseasedup.logit.profile.field.StringField;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ProfileManager extends LogItCoreObject
{
    public ProfileManager(File path, Map<String, Object> fields)
    {
        if (path == null || fields == null)
            throw new IllegalArgumentException();
        
        this.path = path;
        
        for (Entry<String, Object> e : fields.entrySet())
        {
            String fieldName = e.getKey();
            String fieldDefinition = e.getValue().toString();
            
            try
            {
                definedFields.add(newField(fieldName, fieldDefinition));
            }
            catch (RuntimeException ex)
            {
                log(Level.WARNING, "Invalid field definition."
                        + " Field name: " + fieldName + "."
                        + " Cause: " + ex.getMessage());
            }
        }
    }
    
    @Override
    public void dispose()
    {
        path = null;
        
        if (definedFields != null)
        {
            definedFields.clear();
            definedFields = null;
        }
        
        if (fileCache != null)
        {
            fileCache.clear();
            fileCache = null;
        }
        
        if (configurationCache != null)
        {
            configurationCache.clear();
            configurationCache = null;
        }
    }
    
    public boolean containsProfile(String playerName)
    {
        return new File(path, playerName.toLowerCase() + ".yml").exists();
    }
    
    /**
     * Returns a field with the specified name.
     * 
     * @param fieldName the field name.
     * 
     * @return the field object or {@code null} if no field with this name was found.
     */
    public Field getField(String fieldName)
    {
        for (Field field : definedFields)
        {
            if (field.getName().equals(fieldName))
            {
                return field;
            }
        }
        
        return null;
    }
    
    public List<Field> getDefinedFields()
    {
        return ImmutableList.copyOf(definedFields);
    }
    
    public boolean isFieldDefined(String fieldName)
    {
        for (Field field : definedFields)
        {
            if (field.getName().equals(fieldName))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public Object getProfileObject(String playerName, String fieldName)
    {
        return getProfileConfiguration(playerName).get(fieldName);
    }
    
    public String getProfileString(String playerName, String fieldName)
    {
        return getProfileConfiguration(playerName).getString(fieldName);
    }
    
    public int getProfileInteger(String playerName, String fieldName)
    {
        return getProfileConfiguration(playerName).getInt(fieldName);
    }
    
    public double getProfileFloat(String playerName, String fieldName)
    {
        return getProfileConfiguration(playerName).getDouble(fieldName);
    }   
    
    public void setProfileString(String playerName, String fieldName, String value)
    {
        Field field = getField(fieldName);
        
        if (field instanceof StringField)
        {
            StringField stringField = (StringField) field;
            
            if (value.length() < stringField.getMinLength()
                    || value.length() > stringField.getMaxLength())
                throw new IllegalArgumentException();
        }
        else if (field instanceof SetField)
        {
            SetField setField = (SetField) field;
            
            if (!setField.isAccepted(value))
                throw new IllegalArgumentException();
        }
        else
        {
            throw new RuntimeException("Incompatible field type: "
                                       + field.getClass().getSimpleName());
        }
        
        getProfileConfiguration(playerName).set(fieldName, value);
        saveProfileConfiguration(playerName);
    }
    
    public void setProfileInteger(String playerName, String fieldName, int value)
    {
        Field field = getField(fieldName);
        
        if (!(field instanceof IntegerField))
            throw new RuntimeException("Incompatible field type: "
                                       + field.getClass().getSimpleName());
        
        IntegerField integerField = (IntegerField) field;
        
        if (value < integerField.getMinValue()
                || value > integerField.getMaxValue())
            throw new IllegalArgumentException();
        
        getProfileConfiguration(playerName).set(fieldName, value);
        saveProfileConfiguration(playerName);
    }
    
    public void setProfileFloat(String playerName, String fieldName, double value)
    {
        Field field = getField(fieldName);
        
        if (!(field instanceof FloatField))
            throw new RuntimeException("Incompatible field type: "
                                       + field.getClass().getSimpleName());
        
        FloatField floatField = (FloatField) field;
        
        if (value < floatField.getMinValue()
                || value > floatField.getMaxValue())
            throw new IllegalArgumentException();
        
        getProfileConfiguration(playerName).set(fieldName, value);
        saveProfileConfiguration(playerName);
    }
    
    public void removeProfileObject(String playerName, String fieldName)
    {
        getProfileConfiguration(playerName).set(fieldName, null);
        saveProfileConfiguration(playerName);
    }
    
    public File getPath()
    {
        return path;
    }
    
    private File[] listProfiles()
    {
        return path.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".yml");
            }
        });
    }
    
    private YamlConfiguration getProfileConfiguration(String playerName)
    {
        File profileFile = fileCache.get(playerName);
        
        if (profileFile == null)
        {
            fileCache.put(playerName,
                    profileFile = new File(path, playerName.toLowerCase() + ".yml"));
        }
        
        YamlConfiguration configuration = configurationCache.get(playerName);
        
        if (configuration == null)
        {
            configurationCache.put(playerName,
                    configuration = YamlConfiguration.loadConfiguration(profileFile));
        }
        
        return configuration;
    }
    
    private void saveProfileConfiguration(String playerName)
    {
        YamlConfiguration configuration = getProfileConfiguration(playerName);
        
        try
        {
            configuration.save(fileCache.get(playerName));
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    "Could not save profile configuration with player name: " + playerName, ex);
        }
    }
    
    private Field newField(String fieldName, String definitionString)
    {
        Matcher definitionMatcher = FIELD_DEFINITION_PATTERN.matcher(definitionString);
        
        if (definitionMatcher.find())
        {
            String type = definitionMatcher.group(1).toUpperCase();
            String arguments = definitionMatcher.group(2);
            
            switch (type)
            {
            case "STRING":
            {
                Matcher rangeMatcher = INTEGER_RANGE_PATTERN.matcher(arguments);
                
                if (rangeMatcher.find())
                {
                    return new StringField(fieldName, Integer.parseInt(rangeMatcher.group(1)),
                            Integer.parseInt(rangeMatcher.group(2)));
                }
                else
                {
                    throw new RuntimeException("Malformed argument list.");
                }
            }
            case "INTEGER":
            {
                Matcher rangeMatcher = INTEGER_RANGE_PATTERN.matcher(arguments);
                
                if (rangeMatcher.find())
                {
                    return new IntegerField(fieldName, Integer.parseInt(rangeMatcher.group(1)),
                            Integer.parseInt(rangeMatcher.group(2)));
                }
                else
                {
                    throw new RuntimeException("Malformed argument list.");
                }
            }
            case "FLOAT":
            {
                Matcher rangeMatcher = FLOAT_RANGE_PATTERN.matcher(arguments);
                
                if (rangeMatcher.find())
                {
                    return new FloatField(fieldName, Double.valueOf(rangeMatcher.group(1)),
                            Double.valueOf(rangeMatcher.group(2)));
                }
                else
                {
                    throw new RuntimeException("Malformed argument list.");
                }
            }
            case "SET":
                return new SetField(fieldName, Arrays.asList(arguments.split("(?<!\\\\),")));
                
            default:
                throw new RuntimeException("Unknown field type.");
            }
        }
        else
        {
            throw new RuntimeException("Malformed field definition.");
        }
    }
    
    private final static Pattern FIELD_DEFINITION_PATTERN =
            Pattern.compile("^\\s*([A-Za-z_0-9]+)"   // Type name
                           + "\\s*\\[(.*)\\]\\s*$"); // Constraint arguments 
    
    private final static Pattern INTEGER_RANGE_PATTERN =
            Pattern.compile("^\\s*(-?[0-9]+)"        // Min
                           + "\\s*\\.\\.\\."         // Range operator
                           + "\\s*(-?[0-9]+)\\s*$"); // Max
    
    private final static Pattern FLOAT_RANGE_PATTERN =
            Pattern.compile("^\\s*(-?[0-9]+\\.[0-9]+)"        // Min
                           + "\\s*\\.\\.\\."                  // Range operator
                           + "\\s*(-?[0-9]+\\.[0-9]+)\\s*$"); // Max
    
    private File path;
    private List<Field> definedFields = new LinkedList<>();
    private Map<String, File> fileCache = new HashMap<>();
    private Map<String, YamlConfiguration> configurationCache = new HashMap<>();
}
