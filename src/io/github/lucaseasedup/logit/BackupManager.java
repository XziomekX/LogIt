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
package io.github.lucaseasedup.logit;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.db.AbstractSqlDatabase;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * @author LucasEasedUp
 */
public class BackupManager implements Runnable
{
    public BackupManager(LogItCore core, AbstractSqlDatabase database)
    {
        timer = new Timer(40L);
        timer.start();
        
        this.core = core;
        this.database = database;
    }
    
    @Override
    public void run()
    {
        if (!core.getConfig().getBoolean("backup.schedule.enabled"))
            return;
        
        timer.run();
        
        if (timer.getElapsed() >= (core.getConfig().getInt("backup.schedule.interval") * 60L * 20L))
        {
            try
            {
                createBackup(database);
                
                core.log(INFO, getMessage("CREATE_BACKUP_SUCCESS"));
            }
            catch (IOException|SQLException ex)
            {
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
    public void createBackup(AbstractSqlDatabase database) throws IOException, SQLException
    {
        Date             date = new Date();
        SimpleDateFormat sdf  = new SimpleDateFormat(core.getConfig().getString("backup.filename-format"));
        File             backupPath = new File(core.getPlugin().getDataFolder(), core.getConfig().getString("backup.path"));
        File             backupFile = new File(backupPath, sdf.format(date));
        
        if (!backupPath.mkdir())
            throw new IOException("Could not create backup directory.");
        
        if (!backupFile.createNewFile())
            throw new IOException("Backup already exists.");
        
        try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
        {
            backupDatabase.connect();
            backupDatabase.createTable(core.getConfig().getString("storage.accounts.table"), core.getStorageColumns());
            
            try (ResultSet rs = database.select(core.getConfig().getString("storage.accounts.table"), new String[]{"*"}))
            {
                assert rs.getMetaData().getColumnCount() == 1;
                
                while (rs.next())
                {
                    backupDatabase.insert(core.getConfig().getString("storage.accounts.table"), new String[]{
                        core.getConfig().getString("storage.accounts.columns.username"),
                        core.getConfig().getString("storage.accounts.columns.salt"),
                        core.getConfig().getString("storage.accounts.columns.password"),
                        core.getConfig().getString("storage.accounts.columns.ip"),
                        core.getConfig().getString("storage.accounts.columns.last_active")
                    }, new String[]{
                        rs.getString(core.getConfig().getString("storage.accounts.columns.username")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.salt")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.password")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.ip")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.last_active"))
                    });
                }
            }
        }
    }
    
    /**
     * Restores a backup with the specified filename in the directory specified in the config.
     * 
     * @param database Database to be affected by the backup.
     * @param filename Backup filename.
     * @throws FileNotFoundException Thrown if the backup does not exist.
     * @throws SQLException
     */
    public void restoreBackup(AbstractSqlDatabase database, String filename) throws FileNotFoundException, SQLException
    {
        File backupFile = getBackup(filename);
        
        try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
        {
            backupDatabase.connect();
            
            // Clear the table before restoring.
            database.truncateTable(core.getConfig().getString("storage.accounts.table"));
            
            try (ResultSet rs = backupDatabase.select(core.getConfig().getString("storage.accounts.table"), new String[]{"*"}))
            {
                assert rs.getMetaData().getColumnCount() == 1;
                
                while (rs.next())
                {
                    database.insert(core.getConfig().getString("storage.accounts.table"), new String[]{
                        core.getConfig().getString("storage.accounts.columns.username"),
                        core.getConfig().getString("storage.accounts.columns.salt"),
                        core.getConfig().getString("storage.accounts.columns.password"),
                        core.getConfig().getString("storage.accounts.columns.ip"),
                        core.getConfig().getString("storage.accounts.columns.last_active")
                    }, new String[]{
                        rs.getString(core.getConfig().getString("storage.accounts.columns.username")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.salt")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.password")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.ip")),
                        rs.getString(core.getConfig().getString("storage.accounts.columns.last_active"))
                    });
                }
            }
        }
    }
    
    /**
     * Restores the newest backup in the directory specified in the config.
     * 
     * @param database Database to be affected by the backup.
     * @throws FileNotFoundException Thrown if there are no backups.
     * @throws SQLException
     */
    public void restoreBackup(AbstractSqlDatabase database) throws FileNotFoundException, SQLException
    {
        File[] backups = getBackups();
        
        if (backups.length == 0)
            throw new FileNotFoundException();
        
        restoreBackup(database, backups[0].getName());
    }
    
    /**
     * Removes a certain amount of backups starting from the oldest.
     * 
     * @param amount Amount of backups to remove.
     * @throws IOException
     */
    public void removeBackups(int amount) throws IOException
    {
        File[] backups = getBackups();
        
        for (int i = 0; i < amount; i++)
        {
            if (i < backups.length)
            {
                backups[i].delete();
            }
        }
    }
    
    public File[] getBackups()
    {
        File   backupPath = new File(core.getPlugin().getDataFolder(), core.getConfig().getString("backup.path"));
        File[] backups = backupPath.listFiles();
        
        if (backups == null)
            return new File[0];
        
        // Sort backups alphabetically.
        Arrays.sort(backups);
        
        return backups;
    }
    
    /**
     * Searches for a backup with the given filename.
     * 
     * @param filename Backup filename.
     * @return Backup file.
     * @throws FileNotFoundException If no backup with the given filename exists.
     */
    public File getBackup(String filename) throws FileNotFoundException
    {
        File backupPath = new File(core.getPlugin().getDataFolder(), core.getConfig().getString("backup.path"));
        File backup = new File(backupPath, filename);
        
        if (!backup.exists())
            throw new FileNotFoundException();
        
        return backup;
    }
    
    private final LogItCore core;
    private final AbstractSqlDatabase database;
    private final Timer timer;
}
