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
package io.github.lucaseasedup.logit;

import java.util.List;

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
        
        plugin.getConfig().set("locale",                                  plugin.getConfig().getString("locale", "en"));
        plugin.getConfig().set("log-to-file.enabled",                     plugin.getConfig().getBoolean("log-to-file.enabled", false));
        plugin.getConfig().set("log-to-file.filename",                    plugin.getConfig().getString("log-to-file.filename", "debug.log"));
        plugin.getConfig().set("force-login.global",                      plugin.getConfig().getBoolean("force-login.global", true));
        plugin.getConfig().set("force-login.in-worlds",                   plugin.getConfig().getStringList("force-login.in-worlds"));
        plugin.getConfig().set("force-login.allowed-commands",            plugin.getConfig().getStringList("force-login.allowed-commands"));
        plugin.getConfig().set("force-login.timeout",                     plugin.getConfig().getInt("force-login.timeout", -1));
        plugin.getConfig().set("force-login.prevent.move",                plugin.getConfig().getBoolean("force-login.prevent.move", true));
        plugin.getConfig().set("force-login.prevent.toggle-sneak",        plugin.getConfig().getBoolean("force-login.prevent.toggle-sneak", true)); 
        plugin.getConfig().set("force-login.prevent.block-place",         plugin.getConfig().getBoolean("force-login.prevent.block-place", true));
        plugin.getConfig().set("force-login.prevent.block-break",         plugin.getConfig().getBoolean("force-login.prevent.block-break", true));
        plugin.getConfig().set("force-login.prevent.damage-in",           plugin.getConfig().getBoolean("force-login.prevent.damage-in", true));
        plugin.getConfig().set("force-login.prevent.damage-out",          plugin.getConfig().getBoolean("force-login.prevent.damage-out", true));
        plugin.getConfig().set("force-login.prevent.regain-health",       plugin.getConfig().getBoolean("force-login.prevent.regain-health", true)); 
        plugin.getConfig().set("force-login.prevent.food-level-change",   plugin.getConfig().getBoolean("force-login.prevent.food-level-change", true)); 
        plugin.getConfig().set("force-login.prevent.entity-target",       plugin.getConfig().getBoolean("force-login.prevent.entity-target", true)); 
        plugin.getConfig().set("force-login.prevent.chat",                plugin.getConfig().getBoolean("force-login.prevent.chat", true));
        plugin.getConfig().set("force-login.prevent.command-preprocess",  plugin.getConfig().getBoolean("force-login.prevent.command-preprocess", true)); 
        plugin.getConfig().set("force-login.prevent.pickup-item",         plugin.getConfig().getBoolean("force-login.prevent.pickup-item", true));
        plugin.getConfig().set("force-login.prevent.drop-item",           plugin.getConfig().getBoolean("force-login.prevent.drop-item", true));
        plugin.getConfig().set("force-login.prevent.interact",            plugin.getConfig().getBoolean("force-login.prevent.interact", true));
        plugin.getConfig().set("force-login.prevent.interact-entity",     plugin.getConfig().getBoolean("force-login.prevent.interact-entity", true)); 
        plugin.getConfig().set("force-login.prevent.inventory-click",     plugin.getConfig().getBoolean("force-login.prevent.inventory-click", true)); 
        plugin.getConfig().set("force-login.prevent.air-depletion",       plugin.getConfig().getBoolean("force-login.prevent.air-depletion", true)); 
        plugin.getConfig().set("force-login.hide-inventory",              plugin.getConfig().getBoolean("force-login.hide-inventory", false)); 
        plugin.getConfig().set("session-lifetime",                        plugin.getConfig().getInt("session-lifetime", 0));
        plugin.getConfig().set("username.regex",                          plugin.getConfig().getString("username.regex", "[A-Za-z0-9_]+"));
        plugin.getConfig().set("username.min-length",                     plugin.getConfig().getInt("username.min-length", 2));
        plugin.getConfig().set("username.max-length",                     plugin.getConfig().getInt("username.max-length", 16));
        plugin.getConfig().set("username.prohibited-usernames",           plugin.getConfig().getStringList("username.prohibited-usernames"));
        plugin.getConfig().set("password.min-length",                     plugin.getConfig().getInt("password.min-length", 3));
        plugin.getConfig().set("password.max-length",                     plugin.getConfig().getInt("password.max-length", 25));
        plugin.getConfig().set("password.global-password",                plugin.getConfig().getString("password.global-password", ""));
        plugin.getConfig().set("login-fails-to-kick",                     plugin.getConfig().getInt("login-fails-to-kick", -1));
        plugin.getConfig().set("login-fails-to-ban",                      plugin.getConfig().getInt("login-fails-to-ban", -1));
        plugin.getConfig().set("kick-unregistered",                       plugin.getConfig().getBoolean("kick-unregistered", false));
        plugin.getConfig().set("days-of-absence-to-unregister",           plugin.getConfig().getInt("days-of-absence-to-unregister", -1));
        plugin.getConfig().set("preserve-slots.amount",                   plugin.getConfig().getInt("preserve-slots.amount", 0));
        plugin.getConfig().set("preserve-slots.players",                  plugin.getConfig().getStringList("preserve-slots.players"));
        plugin.getConfig().set("accounts-per-ip",                         plugin.getConfig().getInt("accounts-per-ip", 3));
        plugin.getConfig().set("unrestricted-ips",                        plugin.getConfig().getStringList("unrestricted-ips"));
        plugin.getConfig().set("hashing-algorithm",                       plugin.getConfig().getString("hashing-algorithm", "sha-256"));
        plugin.getConfig().set("reveal-spawn-world",                      plugin.getConfig().getBoolean("reveal-spawn-world", true));
        plugin.getConfig().set("waiting-room.enabled",                    plugin.getConfig().getBoolean("waiting-room.enabled", false));
        plugin.getConfig().set("waiting-room.location.world",             plugin.getConfig().getString("waiting-room.location.world", "world"));
        plugin.getConfig().set("waiting-room.location.x",                 plugin.getConfig().getDouble("waiting-room.location.x", 0.0));
        plugin.getConfig().set("waiting-room.location.y",                 plugin.getConfig().getDouble("waiting-room.location.y", 0.0));
        plugin.getConfig().set("waiting-room.location.z",                 plugin.getConfig().getDouble("waiting-room.location.z", 0.0));
        plugin.getConfig().set("waiting-room.location.yaw",               plugin.getConfig().getDouble("waiting-room.location.yaw", 0.0));
        plugin.getConfig().set("waiting-room.location.pitch",             plugin.getConfig().getDouble("waiting-room.location.pitch", 0.0));
        plugin.getConfig().set("groups.enabled",                          plugin.getConfig().getBoolean("groups.enabled", false));
        plugin.getConfig().set("groups.logged-in",                        plugin.getConfig().getString("groups.logged-in", "LoggedIn"));
        plugin.getConfig().set("groups.logged-out",                       plugin.getConfig().getString("groups.logged-out", "LoggedOut"));
        plugin.getConfig().set("integration",                             plugin.getConfig().getString("integration", "none"));
        plugin.getConfig().set("integration-phpbb.logit-script",          plugin.getConfig().getString("integration-phpbb.logit-script", ""));
        plugin.getConfig().set("storage.accounts.db-type",                plugin.getConfig().getString("storage.accounts.db-type", "sqlite"));
        plugin.getConfig().set("storage.accounts.sqlite.filename",        plugin.getConfig().getString("storage.accounts.sqlite.filename", "accounts.db"));
        plugin.getConfig().set("storage.accounts.mysql.host",             plugin.getConfig().getString("storage.accounts.mysql.host", "jdbc:mysql://localhost:3306/")); 
        plugin.getConfig().set("storage.accounts.mysql.user",             plugin.getConfig().getString("storage.accounts.mysql.user", "root"));
        plugin.getConfig().set("storage.accounts.mysql.password",         plugin.getConfig().getString("storage.accounts.mysql.password", ""));
        plugin.getConfig().set("storage.accounts.mysql.database",         plugin.getConfig().getString("storage.accounts.mysql.database", ""));
        plugin.getConfig().set("storage.accounts.table",                  plugin.getConfig().getString("storage.accounts.table", "logit"));
        plugin.getConfig().set("storage.accounts.columns.username",       plugin.getConfig().getString("storage.accounts.columns.username", "username"));
        plugin.getConfig().set("storage.accounts.columns.salt",           plugin.getConfig().getString("storage.accounts.columns.salt", "salt"));
        plugin.getConfig().set("storage.accounts.columns.password",       plugin.getConfig().getString("storage.accounts.columns.password", "password"));
        plugin.getConfig().set("storage.accounts.columns.ip",             plugin.getConfig().getString("storage.accounts.columns.ip", "ip"));
        plugin.getConfig().set("storage.accounts.columns.last_active",    plugin.getConfig().getString("storage.accounts.columns.last_active", "last_active"));
        plugin.getConfig().set("storage.accounts.columns.location_world", plugin.getConfig().getString("storage.accounts.columns.location_world", "location_world"));
        plugin.getConfig().set("storage.accounts.columns.location_x",     plugin.getConfig().getString("storage.accounts.columns.location_x", "location_x"));
        plugin.getConfig().set("storage.accounts.columns.location_y",     plugin.getConfig().getString("storage.accounts.columns.location_y", "location_y"));
        plugin.getConfig().set("storage.accounts.columns.location_z",     plugin.getConfig().getString("storage.accounts.columns.location_z", "location_z"));
        plugin.getConfig().set("storage.accounts.columns.location_yaw",   plugin.getConfig().getString("storage.accounts.columns.location_yaw", "location_yaw"));
        plugin.getConfig().set("storage.accounts.columns.location_pitch", plugin.getConfig().getString("storage.accounts.columns.location_pitch", "location_pitch"));
        plugin.getConfig().set("storage.accounts.columns.in_wr",          plugin.getConfig().getString("storage.accounts.columns.in_wr", "in_wr"));
        plugin.getConfig().set("storage.inventories.filename",            plugin.getConfig().getString("storage.inventories.filename", "inventories.db"));
        plugin.getConfig().set("backup.path",                             plugin.getConfig().getString("backup.path", "backup"));
        plugin.getConfig().set("backup.filename-format",                  plugin.getConfig().getString("backup.filename-format", "yyyy-MM-dd_HH-mm-ss'.db'"));
        plugin.getConfig().set("backup.schedule.enabled",                 plugin.getConfig().getBoolean("backup.schedule.enabled", false));
        plugin.getConfig().set("backup.schedule.interval",                plugin.getConfig().getInt("backup.schedule.interval", 120));
        
        plugin.saveConfig();
    }
    
    public void save()
    {
        plugin.saveConfig();
    }
    
    public Object get(String path)
    {
        return plugin.getConfig().get(path);
    }
    
    public String getString(String path)
    {
        return plugin.getConfig().getString(path);
    }
    
    public boolean getBoolean(String path)
    {
        return plugin.getConfig().getBoolean(path);
    }
    
    public int getInt(String path)
    {
        return plugin.getConfig().getInt(path);
    }
    
    public double getDouble(String path)
    {
        return plugin.getConfig().getDouble(path);
    }
    
    public List<String> getStringList(String path)
    {
        return plugin.getConfig().getStringList(path);
    }
    
    public void set(String path, Object value)
    {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }
    
    private final LogItPlugin plugin;
}
