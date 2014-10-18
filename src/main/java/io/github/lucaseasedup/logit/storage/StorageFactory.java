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
            return new H2Storage("jdbc:h2:" + new File(core.getDataFolder(),
                    configuration.getString(path + ".h2.filename")).getAbsolutePath());
            
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
