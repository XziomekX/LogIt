/*
 * LogItConfiguration.java
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

import java.io.File;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author LucasEasedUp
 */
public final class LogItConfiguration
{
    public LogItConfiguration(LogItPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    /**
     * Loads settings from file and fills missing ones with default values.
     */
    public void load()
    {
        plugin.reloadConfig();
        
        plugin.getConfig().set("stop-if.online-mode-enabled",            plugin.getConfig().getBoolean("stop-if.online-mode-enabled", true));
        plugin.getConfig().set("locale",                                 plugin.getConfig().getString("locale", "en"));
        plugin.getConfig().set("log-to-file.enabled",                    plugin.getConfig().getBoolean("log-to-file.enabled", false));
        plugin.getConfig().set("log-to-file.filename",                   plugin.getConfig().getString("log-to-file.filename", "debug.log"));
        plugin.getConfig().set("force-login.global",                     plugin.getConfig().getBoolean("force-login.global", true));
        plugin.getConfig().set("force-login.in-worlds",                  plugin.getConfig().getStringList("force-login.in-worlds"));
        plugin.getConfig().set("force-login.allowed-commands",           plugin.getConfig().getStringList("force-login.allowed-commands"));
        plugin.getConfig().set("force-login.timeout",                    plugin.getConfig().getInt("force-login.timeout", 30));
        plugin.getConfig().set("force-login.prevent.move",               plugin.getConfig().getBoolean("force-login.prevent.move", true));
        plugin.getConfig().set("force-login.prevent.toggle-sneak",       plugin.getConfig().getBoolean("force-login.prevent.toggle-sneak", true)); 
        plugin.getConfig().set("force-login.prevent.block-place",        plugin.getConfig().getBoolean("force-login.prevent.block-place", true));
        plugin.getConfig().set("force-login.prevent.block-break",        plugin.getConfig().getBoolean("force-login.prevent.block-break", true));
        plugin.getConfig().set("force-login.prevent.damage-in",          plugin.getConfig().getBoolean("force-login.prevent.damage-in", true));
        plugin.getConfig().set("force-login.prevent.damage-out",         plugin.getConfig().getBoolean("force-login.prevent.damage-out", true));
        plugin.getConfig().set("force-login.prevent.regain-health",      plugin.getConfig().getBoolean("force-login.prevent.regain-health", true)); 
        plugin.getConfig().set("force-login.prevent.food-level-change",  plugin.getConfig().getBoolean("force-login.prevent.food-level-change", true)); 
        plugin.getConfig().set("force-login.prevent.entity-target",      plugin.getConfig().getBoolean("force-login.prevent.entity-target", true)); 
        plugin.getConfig().set("force-login.prevent.chat",               plugin.getConfig().getBoolean("force-login.prevent.chat", true));
        plugin.getConfig().set("force-login.prevent.command-preprocess", plugin.getConfig().getBoolean("force-login.prevent.command-preprocess", true)); 
        plugin.getConfig().set("force-login.prevent.pickup-item",        plugin.getConfig().getBoolean("force-login.prevent.pickup-item", true));
        plugin.getConfig().set("force-login.prevent.drop-item",          plugin.getConfig().getBoolean("force-login.prevent.drop-item", true));
        plugin.getConfig().set("force-login.prevent.interact",           plugin.getConfig().getBoolean("force-login.prevent.interact", true));
        plugin.getConfig().set("force-login.prevent.interact-entity",    plugin.getConfig().getBoolean("force-login.prevent.interact-entity", true)); 
        plugin.getConfig().set("force-login.prevent.inventory-click",    plugin.getConfig().getBoolean("force-login.prevent.inventory-click", true)); 
        plugin.getConfig().set("force-login.prevent.air-depletion",      plugin.getConfig().getBoolean("force-login.prevent.air-depletion", true)); 
        plugin.getConfig().set("session-lifetime",                       plugin.getConfig().getInt("session-lifetime", 0));
        plugin.getConfig().set("username.regex",                         plugin.getConfig().getString("username.regex", "[A-Za-z0-9_]+"));
        plugin.getConfig().set("username.min-length",                    plugin.getConfig().getInt("username.min-length", 2));
        plugin.getConfig().set("username.max-length",                    plugin.getConfig().getInt("username.max-length", 16));
        plugin.getConfig().set("username.prohibited-usernames",          plugin.getConfig().getStringList("username.prohibited-usernames"));
        plugin.getConfig().set("password.min-length",                    plugin.getConfig().getInt("password.min-length", 3));
        plugin.getConfig().set("password.max-length",                    plugin.getConfig().getInt("password.max-length", 25));
        plugin.getConfig().set("password.global-password",               plugin.getConfig().getString("password.global-password", ""));
        plugin.getConfig().set("login-fails-to-kick",                    plugin.getConfig().getInt("login-fails-to-kick", 3));
        plugin.getConfig().set("login-fails-to-ban",                     plugin.getConfig().getInt("login-fails-to-ban", 9));
        plugin.getConfig().set("kick-unregistered",                      plugin.getConfig().getBoolean("kick-unregistered", false));
        plugin.getConfig().set("accounts-per-ip",                        plugin.getConfig().getInt("accounts-per-ip", 3));
        plugin.getConfig().set("hashing-algorithm",                      plugin.getConfig().getString("hashing-algorithm", "md5"));
        plugin.getConfig().set("show-spawn-world-info",                  plugin.getConfig().getBoolean("show-spawn-world-info", true));
        plugin.getConfig().set("waiting-room.enabled",                   plugin.getConfig().getBoolean("waiting-room.enabled", false));
        plugin.getConfig().set("waiting-room.location.world",            plugin.getConfig().getString("waiting-room.location.world", "world"));
        plugin.getConfig().set("waiting-room.location.x",                plugin.getConfig().getDouble("waiting-room.location.x", 0.0));
        plugin.getConfig().set("waiting-room.location.y",                plugin.getConfig().getDouble("waiting-room.location.y", 0.0));
        plugin.getConfig().set("waiting-room.location.z",                plugin.getConfig().getDouble("waiting-room.location.z", 0.0));
        plugin.getConfig().set("waiting-room.location.yaw",              plugin.getConfig().getDouble("waiting-room.location.yaw", 0.0));
        plugin.getConfig().set("waiting-room.location.pitch",            plugin.getConfig().getDouble("waiting-room.location.pitch", 0.0));
        plugin.getConfig().set("storage.type",                           plugin.getConfig().getString("storage.type", "sqlite"));
        plugin.getConfig().set("storage.sqlite.filename",                plugin.getConfig().getString("storage.sqlite.filename", "LogIt.db"));
        plugin.getConfig().set("storage.mysql.host",                     plugin.getConfig().getString("storage.mysql.host", "jdbc:mysql://localhost:3306/")); 
        plugin.getConfig().set("storage.mysql.user",                     plugin.getConfig().getString("storage.mysql.user", "root"));
        plugin.getConfig().set("storage.mysql.password",                 plugin.getConfig().getString("storage.mysql.password", ""));
        plugin.getConfig().set("storage.mysql.database",                 plugin.getConfig().getString("storage.mysql.database", ""));
        plugin.getConfig().set("storage.table",                          plugin.getConfig().getString("storage.table", "logit"));
        plugin.getConfig().set("backup.path",                            plugin.getConfig().getString("backup.path", "backup"));
        plugin.getConfig().set("backup.file-format",                     plugin.getConfig().getString("backup.file-format", "yyyy-MM-dd_HH-mm-ss'.db'"));
        plugin.getConfig().set("backup.schedule.enabled",                plugin.getConfig().getBoolean("backup.schedule.enabled", false));
        plugin.getConfig().set("backup.schedule.interval",               plugin.getConfig().getInt("backup.schedule.interval", 7200));
        
        plugin.saveConfig();
    }
    
    /**
     * Saves configuration.
     */
    public void save()
    {
        plugin.saveConfig();
    }
    
    public boolean isStopOnOnlineModeEnabled()
    {
        return plugin.getConfig().getBoolean("stop-if.online-mode-enabled");
    }
    
    public String getLocale()
    {
        return plugin.getConfig().getString("locale");
    }
    
    public boolean isLogToFileEnabled()
    {
        return plugin.getConfig().getBoolean("log-to-file.enabled");
    }
    
    public String getLogFilename()
    {
        return plugin.getConfig().getString("log-to-file.filename");
    }
    
    public boolean getForceLoginGlobal()
    {
        return plugin.getConfig().getBoolean("force-login.global");
    }
    
    public List<String> getForceLoginInWorlds()
    {
        return plugin.getConfig().getStringList("force-login.in-worlds");
    }
    
    public boolean getForceLoginInWorld(World world)
    {
        return getForceLoginInWorlds().contains(world.getName());
    }
    
    public List<String> getForceLoginAllowedCommands()
    {
        return plugin.getConfig().getStringList("force-login.allowed-commands");
    }
    
    public long getForceLoginTimeout()
    {
        return plugin.getConfig().getInt("force-login.timeout") * 20L;
    }
    
    public boolean getForceLoginPreventMove()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.move");
    }
    
    public boolean getForceLoginPreventToggleSneak()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.toggle-sneak");
    }
    
    public boolean getForceLoginPreventBlockPlace()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.block-place");
    }
    
    public boolean getForceLoginPreventBlockBreak()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.block-break");
    }
    
    public boolean getForceLoginPreventDamageIn()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.damage-in");
    }
    
    public boolean getForceLoginPreventDamageOut()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.damage-out");
    }
    
    public boolean getForceLoginPreventRegainHealth()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.regain-health");
    }
    
    public boolean getForceLoginPreventFoodLevelChange()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.food-level-change");
    }
    
    public boolean getForceLoginPreventEntityTarget()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.entity-target");
    }
    
    public boolean getForceLoginPreventChat()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.chat");
    }
    
    public boolean getForceLoginPreventCommandPreprocess()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.command-preprocess");
    }
    
    public boolean getForceLoginPreventPickupItem()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.pickup-item");
    }
    
    public boolean getForceLoginPreventDropItem()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.drop-item");
    }
    
    public boolean getForceLoginPreventInteract()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.interact");
    }
    
    public boolean getForceLoginPreventInteractEntity()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.interact-entity");
    }
    
    public boolean getForceLoginPreventInventoryClick()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.inventory-click");
    }
    
    public boolean getForceLoginPreventAirDepletion()
    {
        return plugin.getConfig().getBoolean("force-login.prevent.air-depletion");
    }
    
    public long getSessionLifetime()
    {
        return plugin.getConfig().getInt("session-lifetime") * 20L;
    }
    
    public String getUsernameRegex()
    {
        return plugin.getConfig().getString("username.regex");
    }
    
    public int getUsernameMinLength()
    {
        return plugin.getConfig().getInt("username.min-length");
    }
    
    public int getUsernameMaxLength()
    {
        return plugin.getConfig().getInt("username.max-length");
    }
    
    public List<String> getProhibitedUsernames()
    {
        return plugin.getConfig().getStringList("username.prohibited-usernames");
    }
    
    public int getPasswordMinLength()
    {
        return plugin.getConfig().getInt("password.min-length");
    }
    
    public int getPasswordMaxLength()
    {
        return plugin.getConfig().getInt("password.max-length");
    }
    
    /**
     * Returns the global password.
     * 
     * Please, note that this may not be a plain form of a password.
     * It is hashed using a hashing algorithm specified in the config.
     * 
     * @return Global password.
     */
    public String getGlobalPassword()
    {
        return plugin.getConfig().getString("password.global-password");
    }
    
    public int getLoginFailsToKick()
    {
        return plugin.getConfig().getInt("login-fails-to-kick");
    }
    
    public int getLoginFailsToBan()
    {
        return plugin.getConfig().getInt("login-fails-to-ban");
    }
    
    public boolean getKickUnregistered()
    {
        return plugin.getConfig().getBoolean("kick-unregistered");
    }
    
    public int getAccountsPerIp()
    {
        return plugin.getConfig().getInt("accounts-per-ip");
    }
    
    public HashingAlgorithm getHashingAlgorithm()
    {
        String s = plugin.getConfig().getString("hashing-algorithm");
        
        if (s.equalsIgnoreCase("plain"))
        {
            return HashingAlgorithm.PLAIN;
        }
        else if (s.equalsIgnoreCase("md2"))
        {
            return HashingAlgorithm.MD2;
        }
        else if (s.equalsIgnoreCase("md5"))
        {
            return HashingAlgorithm.MD5;
        }
        else if (s.equalsIgnoreCase("sha-1"))
        {
            return HashingAlgorithm.SHA1;
        }
        else if (s.equalsIgnoreCase("sha-256"))
        {
            return HashingAlgorithm.SHA256;
        }
        else if (s.equalsIgnoreCase("sha-384"))
        {
            return HashingAlgorithm.SHA384;
        }
        else if (s.equalsIgnoreCase("sha-512"))
        {
            return HashingAlgorithm.SHA512;
        }
        else if (s.equalsIgnoreCase("whirlpool"))
        {
            return HashingAlgorithm.WHIRLPOOL;
        }
        else
        {
            return HashingAlgorithm.UNKNOWN;
        }
    }
    
    public boolean isShowSpawnWorldInfoEnabled()
    {
        return plugin.getConfig().getBoolean("show-spawn-world-info");
    }
    
    public boolean isWaitingRoomEnabled()
    {
        return plugin.getConfig().getBoolean("waiting-room.enabled");
    }
    
    public Location getWaitingRoomLocation()
    {
        World  world = Bukkit.getServer().getWorld(plugin.getConfig().getString("waiting-room.location.world"));
        double x = plugin.getConfig().getDouble("waiting-room.location.x");
        double y = plugin.getConfig().getDouble("waiting-room.location.y");
        double z = plugin.getConfig().getDouble("waiting-room.location.z");
        float  yaw = (float) plugin.getConfig().getDouble("waiting-room.location.yaw");
        float  pitch = (float) plugin.getConfig().getDouble("waiting-room.location.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public String getWaitingRoomLocationWorld()
    {
        return plugin.getConfig().getString("waiting-room.location.world");
    }

    public double getWaitingRoomLocationX()
    {
        return plugin.getConfig().getDouble("waiting-room.location.x");
    }

    public double getWaitingRoomLocationY()
    {
        return plugin.getConfig().getDouble("waiting-room.location.y");
    }

    public double getWaitingRoomLocationZ()
    {
        return plugin.getConfig().getDouble("waiting-room.location.z");
    }

    public float getWaitingRoomLocationYaw()
    {
        return (float) plugin.getConfig().getDouble("waiting-room.location.yaw");
    }

    public float getWaitingRoomLocationPitch()
    {
        return (float) plugin.getConfig().getDouble("waiting-room.location.pitch");
    }
    
    public StorageType getStorageType()
    {
        String s = plugin.getConfig().getString("storage.type");
        
        if (s.equalsIgnoreCase("sqlite"))
        {
            return StorageType.SQLITE;
        }
        else if (s.equalsIgnoreCase("mysql"))
        {
            return StorageType.MYSQL;
        }
        else
        {
            return StorageType.UNKNOWN;
        }
    }
    
    public String getSqliteFilename()
    {
        return plugin.getConfig().getString("storage.sqlite.filename");
    }
    
    public String getMysqlHost()
    {
        return plugin.getConfig().getString("storage.mysql.host");
    }

    public String getMysqlUser()
    {
        return plugin.getConfig().getString("storage.mysql.user");
    }

    public String getMysqlPassword()
    {
        return plugin.getConfig().getString("storage.mysql.password");
    }
    
    public String getMysqlDatabase()
    {
        return plugin.getConfig().getString("storage.mysql.database");
    }
    
    public String getStorageTable()
    {
        return plugin.getConfig().getString("storage.table");
    }
    
    public File getBackupPath()
    {
        return new File(plugin.getDataFolder(), plugin.getConfig().getString("backup.path"));
    }
    
    public String getBackupFileFormat()
    {
        return plugin.getConfig().getString("backup.file-format");
    }
    
    public boolean isScheduledBackupEnabled()
    {
        return plugin.getConfig().getBoolean("backup.schedule.enabled");
    }
    
    public long getScheduledBackupInterval()
    {
        return plugin.getConfig().getInt("backup.schedule.interval") * 20L;
    }
    
    public void setGlobalPassword(String password)
    {
        plugin.getConfig().set("password.global-password", password);
    }
    
    public void setWaitingRoomEnabled(boolean status)
    {
        plugin.getConfig().set("waiting-room.enabled", status);
    }
    
    public void setWaitingRoomLocation(Location location)
    {
        plugin.getConfig().set("waiting-room.location.world", location.getWorld().getName());
        plugin.getConfig().set("waiting-room.location.x", location.getX());
        plugin.getConfig().set("waiting-room.location.y", location.getY());
        plugin.getConfig().set("waiting-room.location.z", location.getZ());
        plugin.getConfig().set("waiting-room.location.yaw", location.getYaw());
        plugin.getConfig().set("waiting-room.location.pitch", location.getPitch());
    }
    
    public static enum HashingAlgorithm
    {
        UNKNOWN, PLAIN, MD2, MD5, SHA1, SHA256, SHA384, SHA512, WHIRLPOOL
    }
    
    public static enum StorageType
    {
        UNKNOWN, SQLITE, MYSQL
    }
    
    private final LogItPlugin plugin;
}
