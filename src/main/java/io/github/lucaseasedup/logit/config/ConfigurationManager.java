package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
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
        if (StringUtils.isBlank(filename)
                || userConfigDef == null || packageConfigDef == null)
        {
            throw new IllegalArgumentException();
        }
        
        if (registrations.containsKey(filename))
            throw new RuntimeException("Configuration already registered: " + filename);
        
        registrations.put(filename,
                new PredefinedConfiguration(filename, userConfigDef, packageConfigDef, header));
    }
    
    public void unregisterConfiguration(String filename)
    {
        if (filename == null)
            throw new IllegalArgumentException();
        
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
        if (filename == null)
            throw new IllegalArgumentException();
        
        return registrations.get(filename);
    }
    
    private Map<String, PredefinedConfiguration> registrations = new HashMap<>();
}
