/*
 * ProfileManager.java
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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ProfileManager extends LogItCoreObject
{
    public ProfileManager(File path, ConfigurationSection fieldsSection)
    {
        this.path = path;
        
        for (String displayName : fieldsSection.getKeys(false))
        {
            String fieldDefinition = fieldsSection.getString(displayName);
            
            if (fieldDefinition != null)
            {
                fields.add(newField(displayName, fieldDefinition));
            }
        }
    }
    
    public List<Field> getDefinedFields()
    {
        return ImmutableList.copyOf(fields);
    }
    
    public Object getProfileObject(String player, String field)
    {
        Configuration profileConfiguration = getProfileConfiguration(player);
        
        if (profileConfiguration == null)
            throw new NullPointerException();
        
        return profileConfiguration.get(field);
    }
    
    public String getProfileString(String player, String field)
    {
        Configuration profileConfiguration = getProfileConfiguration(player);
        
        if (profileConfiguration == null)
            throw new NullPointerException();
        
        return profileConfiguration.getString(field);
    }
    
    public int getProfileInteger(String player, String field)
    {
        Configuration profileConfiguration = getProfileConfiguration(player);
        
        if (profileConfiguration == null)
            throw new NullPointerException();
        
        return profileConfiguration.getInt(field);
    }
    
    public double getProfileFloat(String player, String field)
    {
        Configuration profileConfiguration = getProfileConfiguration(player);
        
        if (profileConfiguration == null)
            throw new NullPointerException();
        
        return profileConfiguration.getDouble(field);
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
    
    private YamlConfiguration getProfileConfiguration(String player)
    {
        File profileFile = new File(path, player.toLowerCase() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(profileFile);
        
        for (Field field : fields)
        {
            if (configuration.get(field.getName()) == null)
            {
                configuration.set(field.getName(), "");
            }
        }
        
        try
        {
            configuration.save(profileFile);
        }
        catch (IOException ex)
        {
        }
        
        return configuration;
    }
    
    private Field newField(String name, String definitionString)
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
                    return new StringField(name, Integer.valueOf(rangeMatcher.group(1)),
                            Integer.valueOf(rangeMatcher.group(2)));
                }
                
                break;
            }
            case "INTEGER":
            {
                Matcher rangeMatcher = INTEGER_RANGE_PATTERN.matcher(arguments);
                
                if (rangeMatcher.find())
                {
                    return new IntegerField(name, Integer.valueOf(rangeMatcher.group(1)),
                            Integer.valueOf(rangeMatcher.group(2)));
                }
                
                break;
            }
            case "FLOAT":
            {
                Matcher rangeMatcher = FLOAT_RANGE_PATTERN.matcher(arguments);
                
                if (rangeMatcher.find())
                {
                    return new FloatField(name, Double.valueOf(rangeMatcher.group(1)),
                            Double.valueOf(rangeMatcher.group(2)));
                }
                
                break;
            }
            case "SET":
                return new SetField(name, arguments.split("(?<!\\\\),"));
            }
        }
        
        throw new RuntimeException("Could not load field definition. Field name: " + name);
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
    
    private final File path;
    private final List<Field> fields = new LinkedList<>();
}
