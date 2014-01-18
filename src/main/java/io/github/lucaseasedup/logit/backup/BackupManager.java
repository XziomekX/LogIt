/*
 * BackupManager.java
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
package io.github.lucaseasedup.logit.backup;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.Timer;
import io.github.lucaseasedup.logit.db.SqliteDatabase;
import io.github.lucaseasedup.logit.db.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class runs a scheduled backup and provides
 * methods for manual backup.
 */
public final class BackupManager extends LogItCoreObject implements Runnable
{
    public BackupManager()
    {
        timer = new Timer(TASK_PERIOD);
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
        File backupDir = getDataFile(getConfig().getString("backup.path"));
        File backupFile = new File(backupDir, sdf.format(new Date()));
        
        backupDir.mkdir();
        
        try
        {
            if (!backupFile.createNewFile())
                throw new IOException("Backup already exists.");
            
            try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
            {
                backupDatabase.connect();
                
                Table sourceTable = getAccountManager().getTable();
                Table backupTable = new Table(backupDatabase, sourceTable.getTableName(),
                        getConfig().getConfigurationSection("storage.accounts.columns"));
                backupTable.open();
                
                List<Map<String, String>> rs = sourceTable.select();
                
                for (Map<String, String> row : rs)
                {
                    Set<String> columns = row.keySet();
                    Collection<String> values = row.values();
                    
                    backupTable.insert(columns.toArray(new String[columns.size()]),
                            values.toArray(new String[values.size()]));
                }
            }
        }
        catch (IOException | SQLException ex)
        {
            log(Level.WARNING, getMessage("CREATE_BACKUP_FAIL"), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Restores a backup.
     * 
     * <p> Automatically reloads the {@code AccountManager} tied to {@code LogItCore}.
     * 
     * @param filename backup filename.
     */
    public void restoreBackup(String filename)
    {
        try
        {
            ReportedException.incrementRequestCount();
            
            File backupFile = getBackup(filename);
            
            try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
            {
                backupDatabase.connect();
                
                Table sourceTable = getAccountManager().getTable();
                Table backupTable = new Table(backupDatabase, sourceTable.getTableName(),
                        getConfig().getConfigurationSection("storage.accounts.columns"));
                backupTable.open();
                
                // Clear the table before restoring.
                sourceTable.truncate();
                
                List<Map<String, String>> rs = backupTable.select();
                
                for (Map<String, String> row : rs)
                {
                    Set<String> columns = row.keySet();
                    Collection<String> values = row.values();
                    
                    sourceTable.insert(columns.toArray(new String[columns.size()]),
                            values.toArray(new String[values.size()]));
                }
            }
            
            getAccountManager().loadAccounts();
        }
        catch (FileNotFoundException | SQLException ex)
        {
            log(Level.WARNING, getMessage("RESTORE_BACKUP_FAIL"), ex);
            
            ReportedException.throwNew(ex);
        }
        catch (ReportedException ex)
        {
            log(Level.WARNING, getMessage("RESTORE_BACKUP_FAIL"), ex);
            
            ex.rethrow();
        }
        finally
        {
            ReportedException.decrementRequestCount();
        }
    }
    
    /**
     * Removes a certain amount of backups starting from the oldest.
     * 
     * @param amount the amount of backups to remove.
     */
    public void removeBackups(int amount)
    {
        File[] backupFiles = getBackups(true);
        
        for (int i = 0; i < amount; i++)
        {
            if (i < backupFiles.length)
            {
                backupFiles[i].delete();
            }
        }
        
        log(Level.INFO, getMessage("REMOVE_BACKUPS_SUCCESS"));
    }
    
    public File[] getBackups(boolean sortAlphabetically)
    {
        File backupDir = getDataFile(getConfig().getString("backup.path"));
        File[] backupFiles = backupDir.listFiles();
        
        if (backupFiles == null)
            return NO_FILES;
        
        if (sortAlphabetically)
        {
            Arrays.sort(backupFiles);
        }
        
        return backupFiles;
    }
    
    public File getBackup(String filename) throws FileNotFoundException
    {
        File backupDir = getDataFile(getConfig().getString("backup.path"));
        File backupFile = new File(backupDir, filename);
        
        if (!backupFile.exists())
            throw new FileNotFoundException();
        
        return backupFile;
    }
    
    public static final long TASK_PERIOD = 40;
    private static final File[] NO_FILES = new File[0];
    
    private final Timer timer;
}
