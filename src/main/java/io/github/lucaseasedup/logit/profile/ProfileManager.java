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

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.File;
import java.io.FilenameFilter;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ProfileManager extends LogItCoreObject
{
    public ProfileManager(File path)
    {
        this.path = path;
    }
    
    public Configuration getProfile(String name)
    {
        File profileFile = new File(path, name.toLowerCase() + ".yml");
        
        if (!profileFile.exists())
            return null;
        
        return YamlConfiguration.loadConfiguration(profileFile);
    }
    
    public File[] listProfiles()
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
    
    public File getPath()
    {
        return path;
    }
    
    private final File path;
}
