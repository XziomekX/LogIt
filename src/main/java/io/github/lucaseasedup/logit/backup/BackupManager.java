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
import io.github.lucaseasedup.logit.account.AccountManager;
import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.Timer;
import io.github.lucaseasedup.logit.storage.SqliteStorage;
import io.github.lucaseasedup.logit.storage.Storage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * This class runs a scheduled backup and provides
 * methods for manual backup.
 */
public final class BackupManager extends LogItCoreObject implements Runnable, Disposable
{
    public BackupManager()
    public BackupManager(AccountManager accountManager)
    {
        timer = new Timer(TASK_PERIOD);
        timer.start();
        
        this.accountManager = accountManager;
    }
    
    @Override
    public void dispose()
    {
        timer = null;
        accountManager = null;
    }
    
    @Override
    public void run()
    {
        if (!getConfig().getBoolean("backup.schedule.enabled"))
            return;
        
        timer.run();
        
        long interval = getConfig().getTime("backup.schedule.interval", TimeUnit.TICKS);
        
        if (timer.getElapsed() >= interval)
        {
            try
            {
                File backupFile = createBackup();
                
                log(Level.INFO, getMessage("CREATE_BACKUP_SUCCESS")
                        .replace("%filename%", backupFile.getName()));
            }
            catch (IOException ex)
            {
                log(Level.WARNING, getMessage("CREATE_BACKUP_FAIL"), ex);
            }
            
            timer.reset();
        }
    }
    
    public File createBackup() throws IOException
    {
        String backupFilenameFormat = getConfig().getString("backup.filename-format");
        SimpleDateFormat sdf = new SimpleDateFormat(backupFilenameFormat);
        File backupDir = getDataFile(getConfig().getString("backup.path"));
        File backupFile = new File(backupDir, sdf.format(new Date()));
        
        backupDir.mkdir();
        
        if (!backupFile.createNewFile())
            throw new IOException("Backup file could not be created.");
        
        List<Storage.Entry> entries =
                accountManager.getStorage().selectEntries(accountManager.getUnit());
        
        try (Storage backupStorage = new SqliteStorage("jdbc:sqlite:" + backupFile))
        {
            backupStorage.connect();
            backupStorage.createUnit(accountManager.getUnit(),
                    accountManager.getStorage().getKeys(accountManager.getUnit()));
            backupStorage.setAutobatchEnabled(true);
            
            for (Storage.Entry entry : entries)
            {
                backupStorage.addEntry(accountManager.getUnit(), entry);
            }
            
            backupStorage.executeBatch();
            backupStorage.clearBatch();
            backupStorage.setAutobatchEnabled(false);
        }
        
        return backupFile;
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
            
            try (Storage backupStorage = new SqliteStorage("jdbc:sqlite:" + backupFile))
            {
                backupStorage.connect();
                
                List<Storage.Entry> entries =
                        backupStorage.selectEntries(accountManager.getUnit());
                
                accountManager.getStorage().eraseUnit(accountManager.getUnit());
                accountManager.getStorage().setAutobatchEnabled(true);
                
                for (Storage.Entry entry : entries)
                {
                    accountManager.getStorage().addEntry(accountManager.getUnit(), entry);
                }
                
                accountManager.getStorage().executeBatch();
                accountManager.getStorage().clearBatch();
                accountManager.getStorage().setAutobatchEnabled(false);
            }
            
            log(Level.INFO, getMessage("RESTORE_BACKUP_SUCCESS"));
        }
        catch (IOException ex)
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
            if (i >= backupFiles.length)
                break;
            
            backupFiles[i].delete();
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
            return null;
        
        return backupFile;
    }
    
    public static final long TASK_PERIOD = 2 * 20;
    private static final File[] NO_FILES = new File[0];
    
    private Timer timer;
    private AccountManager accountManager;
}
