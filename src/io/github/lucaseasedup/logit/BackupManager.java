/*
 * BackupManager.java
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
package io.github.lucaseasedup.logit;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import io.github.lucaseasedup.logit.db.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class runs a scheduled backup and provides
 * methods for manual backup.
 * 
 * @author LucasEasedUp
 */
public final class BackupManager extends LogItCoreObject implements Runnable
{
    /**
     * Sets up a new backup manager.
     * 
     * @param core The LogIt core.
     * @param accounts Account table.
     */
    public BackupManager(LogItCore core)
    {
        super(core);
        
        timer = new Timer(40L);
        timer.start();
    }
    
    @Override
    public void run()
    {
        if (!getConfig().getBoolean("backup.schedule.enabled"))
            return;
        
        timer.run();
        
        if (timer.getElapsed() >= (getConfig().getInt("backup.schedule.interval") * 60L * 20L))
        {
            createBackup();
            
            log(Level.INFO, getMessage("CREATE_BACKUP_SUCCESS"));
            
            timer.reset();
        }
    }
    
    public void createBackup()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(getConfig().getString("backup.filename-format"));
        File backupPath = new File(getDataFolder(), getConfig().getString("backup.path"));
        File backupFile = new File(backupPath, sdf.format(new Date()));
        
        backupPath.mkdir();
        
        try
        {
            if (!backupFile.createNewFile())
                throw new IOException("Backup already exists.");
            
            try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
            {
                backupDatabase.connect();
                
                Table backupTable = new Table(backupDatabase, getAccountManager().getTable().getTableName(),
                        getConfig().getConfigurationSection("storage.accounts.columns"));
                backupTable.open();
                
                List<Map<String, String>> rs = getAccountManager().getTable().select();
                
                for (Map<String, String> m : rs)
                {
                    backupTable.insert(new String[]{
                        "logit.accounts.username",
                        "logit.accounts.salt",
                        "logit.accounts.password",
                        "logit.accounts.ip",
                        "logit.accounts.last_active",
                    }, new String[]{
                        m.get("logit.accounts.username"),
                        m.get("logit.accounts.salt"),
                        m.get("logit.accounts.password"),
                        m.get("logit.accounts.ip"),
                        m.get("logit.accounts.last_active"),
                    });
                }
            }
        }
        catch (IOException | SQLException ex)
        {
            log(Level.WARNING, getMessage("CREATE_BACKUP_FAIL"), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    public void restoreBackup(String filename)
    {
        try
        {
            File backupFile = getBackup(filename);
            
            try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
            {
                backupDatabase.connect();
                
                Table backupTable = new Table(backupDatabase, getAccountManager().getTable().getTableName(),
                        getConfig().getConfigurationSection("storage.accounts.columns"));
                backupTable.open();
                
                // Clear the table before restoring.
                getAccountManager().getTable().truncate();
                
                List<Map<String, String>> rs = backupTable.select();
                
                for (Map<String, String> m : rs)
                {
                    getAccountManager().getTable().insert(new String[]{
                        "logit.accounts.username",
                        "logit.accounts.salt",
                        "logit.accounts.password",
                        "logit.accounts.ip",
                        "logit.accounts.last_active",
                    }, new String[]{
                        m.get("logit.accounts.username"),
                        m.get("logit.accounts.salt"),
                        m.get("logit.accounts.password"),
                        m.get("logit.accounts.ip"),
                        m.get("logit.accounts.last_active"),
                    });
                }
            }
        }
        catch (FileNotFoundException | SQLException ex)
        {
            log(Level.WARNING, getMessage("RESTORE_BACKUP_FAIL"), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Removes a certain amount of backups starting from the oldest.
     * 
     * @param amount Amount of backups to remove.
     */
    public void removeBackups(int amount)
    {
        File[] backups = getBackups(true);
        
        for (int i = 0; i < amount; i++)
        {
            if (i < backups.length)
            {
                backups[i].delete();
            }
        }
        
        log(Level.INFO, getMessage("REMOVE_BACKUPS_SUCCESS"));
    }
    
    public File[] getBackups(boolean sortAlphabetically)
    {
        File   backupPath = new File(getDataFolder(), getConfig().getString("backup.path"));
        File[] backups = backupPath.listFiles();
        
        if (backups == null)
            return new File[0];
        
        if (sortAlphabetically)
        {
            Arrays.sort(backups);
        }
        
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
        File backupPath = new File(getDataFolder(), getConfig().getString("backup.path"));
        File backup = new File(backupPath, filename);
        
        if (!backup.exists())
            throw new FileNotFoundException();
        
        return backup;
    }
    
    private final Timer timer;
}
