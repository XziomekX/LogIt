/*
 * BackupManager.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit;

import static com.gmail.lucaseasedup.logit.LogItPlugin.getMessage;
import com.gmail.lucaseasedup.logit.db.Database;
import com.gmail.lucaseasedup.logit.db.SqliteDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import static java.util.logging.Level.*;

/**
 * @author LucasEasedUp
 */
public class BackupManager implements Runnable
{
    public BackupManager(LogItCore core, Database database)
    {
        timer = new Timer(40L);
        timer.start();
        
        this.core = core;
        this.database = database;
    }
    
    @Override
    public void run()
    {
        if (!core.getConfig().isScheduledBackupEnabled())
            return;
        
        timer.run();
        
        if (timer.getElapsed() >= core.getConfig().getScheduledBackupInterval())
        {
            try
            {
                createBackup(database);
                
                // Notify about the backup.
                core.log(INFO, getMessage("CREATE_BACKUP_SUCCESS"));
            }
            catch (IOException|SQLException ex)
            {
                // Log failure.
                core.log(WARNING, getMessage("CREATE_BACKUP_FAIL"));
            }
            
            timer.reset();
        }
    }
    
    /**
     * Creates a backup of the provided database.
     * 
     * @param database Database to be backed up.
     * @throws IOException
     * @throws SQLException
     */
    public void createBackup(Database database) throws IOException, SQLException
    {
        Date             date = new Date();
        SimpleDateFormat sdf  = new SimpleDateFormat(core.getConfig().getBackupFileFormat());
        File             backupFile = new File(core.getConfig().getBackupPath(), sdf.format(date));
        
        core.getConfig().getBackupPath().mkdir();
        backupFile.createNewFile();
        
        try (SqliteDatabase backupDatabase = new SqliteDatabase())
        {
            backupDatabase.connect("jdbc:sqlite:" + backupFile, null, null, null);
            backupDatabase.create(core.getConfig().getStorageTable(), "username varchar(16) NOT NULL, password varchar(256) NOT NULL, ip varchar(64) NOT NULL");
            
            try (ResultSet rs = database.select(core.getConfig().getStorageTable(), "*"))
            {
                assert rs.getMetaData().getColumnCount() == 1;
                
                while (rs.next())
                {
                    backupDatabase.insert(core.getConfig().getStorageTable(), "\"" + rs.getString("username") + "\", \"" + rs.getString("password") + "\", \"" + rs.getString("ip") + "\"");
                }
            }
        }
    }
    
    /**
     * Removes a certain amount of backups starting from the oldest.
     * 
     * @param amount Amount of backups to delete.
     * @throws IOException
     */
    public void removeBackups(int amount) throws IOException
    {
        File[] backups = core.getConfig().getBackupPath().listFiles();
        
        // Sort backups alphabetically.
        Arrays.sort(backups);
        
        for (int i = 0; i < amount; i++)
        {
            if (i < backups.length)
            {
                backups[i].delete();
            }
        }
    }
    
    /**
     * Restores a backup with the specified filename in the directory specified in the config.
     * 
     * @param database Database to be affected by the backup.
     * @param filename Backup filename.
     * @throws FileNotFoundException When the backup does not exist.
     * @throws SQLException
     */
    public void restoreBackup(Database database, String filename) throws FileNotFoundException, SQLException
    {
        File backupFile = new File(core.getConfig().getBackupPath(), filename);
        
        if (!backupFile.exists())
            throw new FileNotFoundException();
        
        try (SqliteDatabase backupDatabase = new SqliteDatabase())
        {
            backupDatabase.connect("jdbc:sqlite:" + backupFile);
            
            // Clear the table before restoring.
            database.truncate(core.getConfig().getStorageTable());
            
            try (ResultSet rs = backupDatabase.select(core.getConfig().getStorageTable(), "*"))
            {
                assert rs.getMetaData().getColumnCount() == 1;
                
                while (rs.next())
                {
                    database.insert(core.getConfig().getStorageTable(), "\"" + rs.getString("username") + "\", \"" + rs.getString("password") + "\", \"" + rs.getString("ip") + "\"");
                }
            }
        }
    }
    
    /**
     * Restores the newest backup in the directory specified in the config.
     * 
     * @param database Database to be affected by the backup.
     * @throws FileNotFoundException When there are not backups.
     * @throws SQLException
     */
    public void restoreBackup(Database database) throws FileNotFoundException, SQLException
    {
        File[] backups = core.getConfig().getBackupPath().listFiles();
        
        // Sort backups alphabetically.
        Arrays.sort(backups);
        
        this.restoreBackup(database, backups[0].getName());
    }
    
    private final LogItCore core;
    private final Database database;
    private final Timer timer;
}
