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

import static com.gmail.lucaseasedup.logit.LogItConfiguration.HashingAlgorithm.*;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author LucasEasedUp
 */
public class LogItConfiguration
{
    public LogItConfiguration(LogItPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    public void load()
    {
        plugin.reloadConfig();
        
        plugin.getConfig().set("stop-if.online-mode-enabled",           plugin.getConfig().getBoolean("stop-if.online-mode-enabled", true));
        plugin.getConfig().set("locale",                                plugin.getConfig().getString("locale", "en"));
        plugin.getConfig().set("verbose",                               plugin.getConfig().getBoolean("verbose", false));
        plugin.getConfig().set("force-login",                           plugin.getConfig().getBoolean("force-login", true));
        plugin.getConfig().set("force-login-in-worlds",                 plugin.getConfig().getStringList("force-login-in-worlds"));
        plugin.getConfig().set("username.regex",                        plugin.getConfig().getString("username.regex", "[A-Za-z0-9_]+"));
        plugin.getConfig().set("username.min-length",                   plugin.getConfig().getInt("username.min-length", 2));
        plugin.getConfig().set("username.max-length",                   plugin.getConfig().getInt("username.max-length", 16));
        plugin.getConfig().set("username.prohibited-usernames",         plugin.getConfig().getStringList("username.prohibited-usernames"));
        plugin.getConfig().set("password.min-length",                   plugin.getConfig().getInt("password.min-length", 3));
        plugin.getConfig().set("global-password-hash",                  plugin.getConfig().getString("global-password-hash", ""));
        plugin.getConfig().set("login-fails-to-kick",                   plugin.getConfig().getInt("login-fails-to-kick", 3));
        plugin.getConfig().set("hashing-algorithm",                     plugin.getConfig().getString("hashing-algorithm", "md5"));
        plugin.getConfig().set("out-of-session.allowed-commands",       plugin.getConfig().getStringList("out-of-session.allowed-commands"));
        plugin.getConfig().set("out-of-session.timeout",                plugin.getConfig().getLong("out-of-session.timeout", 30L));
        plugin.getConfig().set("out-of-session.event-prevention.move",  plugin.getConfig().getBoolean("out-of-session.event-prevention.move", true));
        plugin.getConfig().set("out-of-session.event-prevention.toggle-sneak", plugin.getConfig().getBoolean("out-of-session.event-prevention.toggle-sneak", true));
        plugin.getConfig().set("out-of-session.event-prevention.block-place", plugin.getConfig().getBoolean("out-of-session.event-prevention.block-place", true));
        plugin.getConfig().set("out-of-session.event-prevention.block-break", plugin.getConfig().getBoolean("out-of-session.event-prevention.block-break", true));
        plugin.getConfig().set("out-of-session.event-prevention.damage-in", plugin.getConfig().getBoolean("out-of-session.event-prevention.damage-in", true));
        plugin.getConfig().set("out-of-session.event-prevention.damage-out", plugin.getConfig().getBoolean("out-of-session.event-prevention.damage-out", true));
        plugin.getConfig().set("out-of-session.event-prevention.regain-health", plugin.getConfig().getBoolean("out-of-session.event-prevention.regain-health", true));
        plugin.getConfig().set("out-of-session.event-prevention.food-level-change", plugin.getConfig().getBoolean("out-of-session.event-prevention.food-level-change", true));
        plugin.getConfig().set("out-of-session.event-prevention.chat",  plugin.getConfig().getBoolean("out-of-session.event-prevention.chat", true));
        plugin.getConfig().set("out-of-session.event-prevention.command-preprocess", plugin.getConfig().getBoolean("out-of-session.event-prevention.command-preprocess", true));
        plugin.getConfig().set("out-of-session.event-prevention.pickup-item", plugin.getConfig().getBoolean("out-of-session.event-prevention.pickup-item", true));
        plugin.getConfig().set("out-of-session.event-prevention.drop-item", plugin.getConfig().getBoolean("out-of-session.event-prevention.drop-item", true));
        plugin.getConfig().set("out-of-session.event-prevention.interact", plugin.getConfig().getBoolean("out-of-session.event-prevention.interact", true));
        plugin.getConfig().set("out-of-session.event-prevention.interact-entity", plugin.getConfig().getBoolean("out-of-session.event-prevention.interact-entity", true));
        plugin.getConfig().set("out-of-session.event-prevention.inventory-click", plugin.getConfig().getBoolean("out-of-session.event-prevention.inventory-click", true));
        plugin.getConfig().set("out-of-session.event-prevention.air-depletion", plugin.getConfig().getBoolean("out-of-session.event-prevention.air-depletion", true));
        plugin.getConfig().set("show-spawn-world-info",                 plugin.getConfig().getBoolean("show-spawn-world-info", true));
        plugin.getConfig().set("waiting-room.enabled",                  plugin.getConfig().getBoolean("waiting-room.enabled", false));
        plugin.getConfig().set("waiting-room.location.world",           plugin.getConfig().getString("waiting-room.location.world", "world"));
        plugin.getConfig().set("waiting-room.location.x",               plugin.getConfig().getDouble("waiting-room.location.x", 0.0));
        plugin.getConfig().set("waiting-room.location.y",               plugin.getConfig().getDouble("waiting-room.location.y", 0.0));
        plugin.getConfig().set("waiting-room.location.z",               plugin.getConfig().getDouble("waiting-room.location.z", 0.0));
        plugin.getConfig().set("waiting-room.location.yaw",             plugin.getConfig().getDouble("waiting-room.location.yaw", 0.0));
        plugin.getConfig().set("waiting-room.location.pitch",           plugin.getConfig().getDouble("waiting-room.location.pitch", 0.0));
        plugin.getConfig().set("storage.type",                          plugin.getConfig().getString("storage.type", "sqlite"));
        plugin.getConfig().set("storage.sqlite.filename",               plugin.getConfig().getString("storage.sqlite.filename", "jdbc:sqlite:plugins/LogIt/LogIt.db"));
        plugin.getConfig().set("storage.mysql.host",                    plugin.getConfig().getString("storage.mysql.host", "jdbc:mysql://localhost:3306/"));
        plugin.getConfig().set("storage.mysql.user",                    plugin.getConfig().getString("storage.mysql.user", "root"));
        plugin.getConfig().set("storage.mysql.password",                plugin.getConfig().getString("storage.mysql.password", ""));
        plugin.getConfig().set("storage.mysql.database",                plugin.getConfig().getString("storage.mysql.database", ""));
        plugin.getConfig().set("storage.table",                         plugin.getConfig().getString("storage.table", "logit"));
        plugin.getConfig().set("storage.columns.username",              plugin.getConfig().getString("storage.columns.username", "username"));
        plugin.getConfig().set("storage.columns.password",              plugin.getConfig().getString("storage.columns.password", "password"));
        
        plugin.saveConfig();
    }
    
    public void reload()
    {
        load();
    }
    
    public void save()
    {
        plugin.saveConfig();
    }
    
    public boolean getStopIfOnlineModeEnabled()
    {
        return plugin.getConfig().getBoolean("stop-if.online-mode-enabled");
    }
    
    public String getLocale()
    {
        return plugin.getConfig().getString("locale");
    }
    
    public boolean getVerbose()
    {
        return plugin.getConfig().getBoolean("verbose");
    }
    
    public boolean getForceLogin()
    {
        return plugin.getConfig().getBoolean("force-login");
    }
    
    public List<String> getForceLoginInWorlds()
    {
        return plugin.getConfig().getStringList("force-login-in-worlds");
    }
    
    public boolean getForceLoginInWorld(World world)
    {
        return getForceLoginInWorlds().contains(world.getName());
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
    
    public List<String> getUsernameProhibitedUsernames()
    {
        return plugin.getConfig().getStringList("username.prohibited-usernames");
    }
    
    public int getPasswordMinLength()
    {
        return plugin.getConfig().getInt("password.min-length");
    }

    public String getGlobalPasswordHash()
    {
        return plugin.getConfig().getString("global-password-hash");
    }
    
    public int getLoginFailsToKick()
    {
        return plugin.getConfig().getInt("login-fails-to-kick");
    }
    
    public HashingAlgorithm getHashingAlgorithm()
    {
        String s = plugin.getConfig().getString("hashing-algorithm");
        
        if (s.equalsIgnoreCase("plain"))
        {
            return PLAIN;
        }
        else if (s.equalsIgnoreCase("md2"))
        {
            return MD2;
        }
        else if (s.equalsIgnoreCase("md5"))
        {
            return MD5;
        }
        else if (s.equalsIgnoreCase("sha-1"))
        {
            return SHA1;
        }
        else if (s.equalsIgnoreCase("sha-256"))
        {
            return SHA256;
        }
        else if (s.equalsIgnoreCase("sha-384"))
        {
            return SHA384;
        }
        else if (s.equalsIgnoreCase("sha-512"))
        {
            return SHA512;
        }
        else if (s.equalsIgnoreCase("whirlpool"))
        {
            return WHIRLPOOL;
        }
        else
            return UNKNOWN;
    }
    
    public List<String> getOutOfSessionAllowedCommands()
    {
        return plugin.getConfig().getStringList("out-of-session.allowed-commands");
    }
    
    public long getOutOfSessionTimeout()
    {
        return (long) plugin.getConfig().getDouble("out-of-session.timeout");
    }
    
    public boolean getOutOfSessionEventPreventionMove()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.move");
    }
    
    public boolean getOutOfSessionEventPreventionToggleSneak()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.toggle-sneak");
    }
    
    public boolean getOutOfSessionEventPreventionBlockPlace()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.block-place");
    }
    
    public boolean getOutOfSessionEventPreventionBlockBreak()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.block-break");
    }
    
    public boolean getOutOfSessionEventPreventionDamageIn()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.damage-in");
    }
    
    public boolean getOutOfSessionEventPreventionDamageOut()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.damage-out");
    }
    
    public boolean getOutOfSessionEventPreventionRegainHealth()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.regain-health");
    }
    
    public boolean getOutOfSessionEventPreventionFoodLevelChange()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.food-level-change");
    }
        
    public boolean getOutOfSessionEventPreventionChat()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.chat");
    }
        
    public boolean getOutOfSessionEventPreventionCommandPreprocess()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.command-preprocess");
    }
        
    public boolean getOutOfSessionEventPreventionPickupItem()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.pickup-item");
    }
        
    public boolean getOutOfSessionEventPreventionDropItem()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.drop-item");
    }
        
    public boolean getOutOfSessionEventPreventionInteract()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.interact");
    }
        
    public boolean getOutOfSessionEventPreventionInteractEntity()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.interact-entity");
    }
        
    public boolean getOutOfSessionEventPreventionInventoryClick()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.inventory-click");
    }
    
    public boolean getOutOfSessionEventPreventionAirDepletion()
    {
        return plugin.getConfig().getBoolean("out-of-session.event-prevention.air-depletion");
    }
    
    public boolean getShowSpawnWorldInfo()
    {
        return plugin.getConfig().getBoolean("show-spawn-world-info");
    }
    
    public boolean getWaitingRoomEnabled()
    {
        return plugin.getConfig().getBoolean("waiting-room.enabled");
    }
    
    public Location getWaitingRoomLocation()
    {
        World world = Bukkit.getServer().getWorld(plugin.getConfig().getString("waiting-room.location.world"));
        double x = plugin.getConfig().getDouble("waiting-room.location.x");
        double y = plugin.getConfig().getDouble("waiting-room.location.y");
        double z = plugin.getConfig().getDouble("waiting-room.location.z");
        float yaw = (float) plugin.getConfig().getDouble("waiting-room.location.yaw");
        float pitch = (float) plugin.getConfig().getDouble("waiting-room.location.pitch");
        
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
            return StorageType.UNKNOWN;
    }
    
    public String getStorageSqliteFilename()
    {
        return plugin.getConfig().getString("storage.sqlite.filename");
    }
    
    public String getStorageMysqlHost()
    {
        return plugin.getConfig().getString("storage.mysql.host");
    }

    public String getStorageMysqlUser()
    {
        return plugin.getConfig().getString("storage.mysql.user");
    }

    public String getStorageMysqlPassword()
    {
        return plugin.getConfig().getString("storage.mysql.password");
    }
    
    public String getStorageMysqlDatabase()
    {
        return plugin.getConfig().getString("storage.mysql.database");
    }
    
    public String getStorageTable()
    {
        return plugin.getConfig().getString("storage.table");
    }
    
    public String getStorageColumnsUsername()
    {
        return plugin.getConfig().getString("storage.columns.username");
    }
    
    public String getStorageColumnsPassword()
    {
        return plugin.getConfig().getString("storage.columns.password");
    }
    
    public void setGlobalPasswordHash(String hash)
    {
        plugin.getConfig().set("global-password-hash", hash);
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
    
    private LogItPlugin plugin;
}