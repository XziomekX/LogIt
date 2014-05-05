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
import java.io.File;
import org.bukkit.configuration.ConfigurationSection;

public final class StorageFactory
{
    private StorageFactory()
    {
    }
    
    public static Storage produceStorage(StorageType type, ConfigurationSection config)
    {
        LogItCore core = LogItCore.getInstance();
        
        switch (type)
        {
        case NONE:
            return new NullStorage();
        
        case SQLITE:
            return new SqliteStorage("jdbc:sqlite:" + core.getDataFolder() + "/"
                    + config.getString("sqlite.filename"));
            
        case MYSQL:
            return new MySqlStorage(
                    config.getString("mysql.host"),
                    config.getString("mysql.user"),
                    config.getString("mysql.password"),
                    config.getString("mysql.database"));
            
        case H2:
            return new H2Storage("jdbc:h2:" + core.getDataFolder()
                    + "/" + config.getString("h2.filename"));
            
        case POSTGRESQL:
            return new PostgreSqlStorage(
                    config.getString("postgresql.host"),
                    config.getString("postgresql.user"),
                    config.getString("postgresql.password"));
            
        case CSV:
        {
            File dir = core.getDataFile(config.getString("csv.dir"));
            
            if (!dir.exists())
                dir.mkdir();
            
            return new CsvStorage(dir);
        }
        default:
            throw new RuntimeException("StorageFactory does not support the "
                                       + type.name() + " storage type.");
        }
    }
}
