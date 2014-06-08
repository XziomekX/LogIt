/*
 * StorageFactory.java
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
package io.github.lucaseasedup.logit.storage;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.config.PredefinedConfiguration;
import java.io.File;

public final class StorageFactory
{
    public StorageFactory(PredefinedConfiguration configuration, String path)
    {
        this.configuration = configuration;
        this.path = path;
    }
    
    public Storage produceStorage(StorageType type)
    {
        LogItCore core = LogItCore.getInstance();
        
        switch (type)
        {
        case NONE:
            return new NullStorage();
        
        case SQLITE:
            return new SqliteStorage("jdbc:sqlite:" + core.getDataFolder() + "/"
                    + configuration.getString(path + ".sqlite.filename"));
            
        case MYSQL:
            return new MySqlStorage(
                    configuration.getString(path + ".mysql.host"),
                    configuration.getString(path + ".mysql.user"),
                    configuration.getString(path + ".mysql.password"),
                    configuration.getString(path + ".mysql.database"));
            
        case H2:
            return new H2Storage("jdbc:h2:" + core.getDataFolder()
                    + "/" + configuration.getString(path + ".h2.filename"));
            
        case POSTGRESQL:
            return new PostgreSqlStorage(
                    configuration.getString(path + ".postgresql.host"),
                    configuration.getString(path + ".postgresql.user"),
                    configuration.getString(path + ".postgresql.password"));
            
        case CSV:
        {
            File dir = core.getDataFile(configuration.getString(path + ".csv.dir"));
            
            if (!dir.exists())
            {
                dir.getParentFile().mkdirs();
                dir.mkdir();
            }
            
            return new CsvStorage(dir);
        }
        default:
            throw new RuntimeException("StorageFactory does not support the "
                                       + type.name() + " storage type.");
        }
    }
    
    private final PredefinedConfiguration configuration;
    private final String path;
}
