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
import java.util.logging.Logger;

/**
 * This class runs a scheduled backup and provides
 * methods for manual backup.
 * 
 * @author LucasEasedUp
 */
public class BackupManager implements Runnable
{
    /**
     * Sets up a new backup manager.
     * 
     * @param core The LogIt core.
     * @param accounts Account table.
     */
    public BackupManager(LogItCore core, Table accounts)
    {
        timer = new Timer(40L);
        timer.start();
        
        this.core = core;
        this.accounts = accounts;
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
                createBackup();
                
                core.log(Level.INFO, getMessage("CREATE_BACKUP_SUCCESS"));
            }
            catch (IOException | SQLException ex)
            {
                Logger.getLogger(BackupManager.class.getName()).log(Level.WARNING, null, ex);
                core.log(Level.WARNING, getMessage("CREATE_BACKUP_FAIL"));
            }
            
            timer.reset();
        }
    }
    
    public void createBackup() throws IOException, SQLException
    {
        Date             date = new Date();
        SimpleDateFormat sdf  = new SimpleDateFormat(core.getConfig().getString("backup.filename-format"));
        File             backupPath = new File(core.getPlugin().getDataFolder(), core.getConfig().getString("backup.path"));
        File             backupFile = new File(backupPath, sdf.format(date));
        
        backupPath.mkdir();
        
        if (!backupFile.createNewFile())
            throw new IOException("Backup already exists.");
        
        try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
        {
            backupDatabase.connect();
            
            Table backupTable = new Table(backupDatabase, accounts.getTableName(),
                    core.getConfig().getConfigurationSection("storage.accounts.columns"));
            backupTable.open();
            
            List<Map<String, String>> rs = accounts.select();
            
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
    
    public void restoreBackup(String filename) throws FileNotFoundException, SQLException
    {
        File backupFile = getBackup(filename);
        
        try (SqliteDatabase backupDatabase = new SqliteDatabase("jdbc:sqlite:" + backupFile))
        {
            backupDatabase.connect();
            
            Table backupTable = new Table(backupDatabase, accounts.getTableName(),
                    core.getConfig().getConfigurationSection("storage.accounts.columns"));
            backupTable.open();
            
            // Clear the table before restoring.
            accounts.truncate();
            
            List<Map<String, String>> rs = backupTable.select();
            
            for (Map<String, String> m : rs)
            {
                accounts.insert(new String[]{
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
    
    /**
     * Removes a certain amount of backups starting from the oldest.
     * 
     * @param amount Amount of backups to remove.
     * @throws IOException
     */
    public void removeBackups(int amount) throws IOException
    {
        File[] backups = getBackups(true);
        
        for (int i = 0; i < amount; i++)
        {
            if (i < backups.length)
            {
                backups[i].delete();
            }
        }
    }
    
    public File[] getBackups(boolean sortAlphabetically)
    {
        File   backupPath = new File(core.getPlugin().getDataFolder(), core.getConfig().getString("backup.path"));
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
        File backupPath = new File(core.getPlugin().getDataFolder(), core.getConfig().getString("backup.path"));
        File backup = new File(backupPath, filename);
        
        if (!backup.exists())
            throw new FileNotFoundException();
        
        return backup;
    }
    
    private final LogItCore core;
    private final Table accounts;
    private final Timer timer;
}
