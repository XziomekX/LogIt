/*
 * ConfigurationManager.java
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

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;

public final class ConfigurationManager extends LogItCoreObject
{
    @Override
    public void dispose()
    {
        if (registrations != null)
        {
            for (PredefinedConfiguration configuration : registrations.values())
            {
                configuration.dispose();
            }
            
            registrations.clear();
            registrations = null;
        }
    }
    
    public void registerConfiguration(String filename,
                                      String userConfigDef,
                                      String packageConfigDef,
                                      String header)
    {
        if (filename == null || userConfigDef == null || packageConfigDef == null)
            throw new IllegalArgumentException();
        
        if (registrations.containsKey(filename))
            throw new RuntimeException("Configuration already registered: " + filename);
        
        registrations.put(filename,
                new PredefinedConfiguration(filename, userConfigDef, packageConfigDef, header));
    }
    
    public void unregisterConfiguration(String filename)
    {
        registrations.remove(filename);
    }
    
    public void unregisterAll()
    {
        registrations.clear();
    }
    
    public void loadAll() throws IOException,
                                 InvalidConfigurationException,
                                 InvalidPropertyValueException
    {
        for (PredefinedConfiguration configuration : registrations.values())
        {
            configuration.load();
        }
    }
    
    public PredefinedConfiguration getConfiguration(String filename)
    {
        return registrations.get(filename);
    }
    
    private Map<String, PredefinedConfiguration> registrations = new HashMap<>();
}
