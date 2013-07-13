/*
 * LogItConfiguration.java
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
package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.LogItPlugin;
import static io.github.lucaseasedup.logit.config.PropertyType.BOOLEAN;
import static io.github.lucaseasedup.logit.config.PropertyType.DOUBLE;
import static io.github.lucaseasedup.logit.config.PropertyType.INT;
import static io.github.lucaseasedup.logit.config.PropertyType.STRING;
import static io.github.lucaseasedup.logit.config.PropertyType.STRING_LIST;
import static io.github.lucaseasedup.logit.config.PropertyType.VECTOR;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author LucasEasedUp
 */
public final class LogItConfiguration extends PropertyObserver
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
        
        addProperty("locale", STRING, false, "en", new String[]{"de", "en", "pl"}, new PropertyObserver()
        {
            @Override
            public void update(Property p)
            {
                try
                {
                    plugin.loadMessages();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(LogItConfiguration.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        });
        
        addProperty("log-to-file.enabled", BOOLEAN, false, false);
        addProperty("log-to-file.filename", STRING, false, "debug.log");
        
        addProperty("force-login.global", BOOLEAN, false, true);
        addProperty("force-login.in-worlds", STRING_LIST, false, new ArrayList<>(0));
        addProperty("force-login.allowed-commands", STRING_LIST, false, new ArrayList<>(0));
        addProperty("force-login.timeout", INT, false, -1);
        addProperty("force-login.prevent.move", BOOLEAN, false, true);
        addProperty("force-login.prevent.toggle-sneak", BOOLEAN, false, true);
        addProperty("force-login.prevent.block-place", BOOLEAN, false, true);
        addProperty("force-login.prevent.block-break", BOOLEAN, false, true);
        addProperty("force-login.prevent.damage-in", BOOLEAN, false, true);
        addProperty("force-login.prevent.damage-out", BOOLEAN, false, true);
        addProperty("force-login.prevent.regain-health", BOOLEAN, false, true);
        addProperty("force-login.prevent.food-level-change", BOOLEAN, false, true);
        addProperty("force-login.prevent.entity-target", BOOLEAN, false, true);
        addProperty("force-login.prevent.chat", BOOLEAN, false, true);
        addProperty("force-login.prevent.command-preprocess", BOOLEAN, false, true); 
        addProperty("force-login.prevent.pickup-item", BOOLEAN, false, true);
        addProperty("force-login.prevent.drop-item", BOOLEAN, false, true);
        addProperty("force-login.prevent.interact", BOOLEAN, false, true);
        addProperty("force-login.prevent.interact-entity", BOOLEAN, false, true);
        addProperty("force-login.prevent.inventory-click", BOOLEAN, false, true);
        addProperty("force-login.prevent.air-depletion", BOOLEAN, false, true);
        addProperty("force-login.hide-inventory", BOOLEAN, false, false);
        
        addProperty("session-lifetime", INT, false, 0);
        addProperty("reveal-spawn-world", BOOLEAN, false, true);
        
        addProperty("username.regex", STRING, false, "[A-Za-z0-9_]+");
        addProperty("username.min-length", INT, false, 2);
        addProperty("username.max-length", INT, false, 16);
        addProperty("username.prohibited-usernames", STRING_LIST, false, new ArrayList<>(0));
        
        addProperty("password.min-length", INT, false, 3);
        addProperty("password.max-length", INT, false, 25);
        addProperty("password.hashing-algorithm", STRING, true, "sha-256", new String[]{
            "plan", "md2", "md5", "sha-1", "sha-256", "sha-384", "sha-512", "whirlpool", "bcrypt"
        });
        addProperty("password.use-salt", BOOLEAN, true, true);
        addProperty("password.global-password", STRING, false, "");
        
        addProperty("login-fails-to-kick", INT, false, -1);
        addProperty("login-fails-to-ban", INT, false, -1);
        addProperty("kick-unregistered", BOOLEAN, false, false);
        addProperty("days-of-absence-to-unregister", INT, false, -1);
        
        addProperty("preserve-slots.amount", INT, false, 0);
        addProperty("preserve-slots.players", STRING_LIST, false, new ArrayList<>(0));
        
        addProperty("accounts-per-ip.amount", INT, false, 3);
        addProperty("accounts-per-ip.unrestricted-ips", STRING_LIST, false, new ArrayList<>(0));
        
        addProperty("password-recovery.enabled", BOOLEAN, true, false);
        addProperty("password-recovery.subject", STRING, false, "Password recovery for %player%");
        addProperty("password-recovery.body-template", STRING, false, "mail/password-recovery.html");
        addProperty("password-recovery.html-enabled", BOOLEAN, false, true);
        addProperty("password-recovery.password-length", INT, false, 6);
        addProperty("password-recovery.password-combination", STRING, false, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        
        PropertyObserver smtpConfigurationObserver = new PropertyObserver()
        {
            @Override
            public void update(Property p)
            {
                plugin.getCore().getMailSender().configure(plugin.getCore().getConfig().getString("mail.smtp-host"),
                    Integer.valueOf(plugin.getCore().getConfig().getString("mail.smtp-port")),
                    plugin.getCore().getConfig().getString("mail.smtp-user"),
                    plugin.getCore().getConfig().getString("mail.smtp-password"));
            }
        };
        
        addProperty("mail.email-address", STRING, false, "");
        addProperty("mail.smtp-host", STRING, false, "", smtpConfigurationObserver);
        addProperty("mail.smtp-port", INT, false, 465, smtpConfigurationObserver);
        addProperty("mail.smtp-user", STRING, false, "", smtpConfigurationObserver);
        addProperty("mail.smtp-password", STRING, false, "", smtpConfigurationObserver);
        
        addProperty("waiting-room.enabled", BOOLEAN, false, false, new PropertyObserver()
        {
            @Override
            public void update(Property p)
            {
                if (!p.getBoolean())
                {
                    plugin.getCore().getWaitingRoom().removeAll();
                }
            }
        });
        addProperty("waiting-room.location.world", STRING, false, "world");
        addProperty("waiting-room.location.position", VECTOR, false, new Vector(0, 0, 0));
        addProperty("waiting-room.location.yaw", DOUBLE, false, 0d);
        addProperty("waiting-room.location.pitch", DOUBLE, false, 0d);
        addProperty("waiting-room.newbie-teleport.enabled", BOOLEAN, false, false);
        addProperty("waiting-room.newbie-teleport.location.world", STRING, false, "world");
        addProperty("waiting-room.newbie-teleport.location.position", VECTOR, false, new Vector(0, 0, 0));
        addProperty("waiting-room.newbie-teleport.location.yaw", DOUBLE, false, 0d);
        addProperty("waiting-room.newbie-teleport.location.pitch", DOUBLE, false, 0d);
        
        addProperty("groups.enabled", BOOLEAN, true, false);
        addProperty("groups.registered", STRING, true, "Registered");
        addProperty("groups.unregistered", STRING, true, "Unregistered");
        addProperty("groups.logged-in", STRING, true, "LoggedIn");
        addProperty("groups.logged-out", STRING, true, "LoggedOut");
        
        addProperty("integration", STRING, true, "none", new String[]{"none", "phpbb"});
        addProperty("integration-phpbb.logit-script", STRING, false, "");
        
        addProperty("storage.accounts.db-type", STRING, true, "sqlite", new String[]{
            "sqlite", "mysql", "h2", "csv"
        });
        addProperty("storage.accounts.sqlite.filename", STRING, true, "accounts.db");
        addProperty("storage.accounts.h2.filename", STRING, true, "accounts");
        addProperty("storage.accounts.mysql.host", STRING, true, "jdbc:mysql://localhost:3306/"); 
        addProperty("storage.accounts.mysql.user", STRING, true, "root");
        addProperty("storage.accounts.mysql.password", STRING, true, "");
        addProperty("storage.accounts.mysql.database", STRING, true, "");
        addProperty("storage.accounts.table", STRING, true, "logit");
        addProperty("storage.accounts.columns.username", STRING, true, "username");
        addProperty("storage.accounts.columns.salt", STRING, true, "salt");
        addProperty("storage.accounts.columns.password", STRING, true, "password");
        addProperty("storage.accounts.columns.ip", STRING, true, "ip");
        addProperty("storage.accounts.columns.email", STRING, true, "email");
        addProperty("storage.accounts.columns.last_active", STRING, true, "last_active");
        addProperty("storage.accounts.columns.location_world", STRING, true, "location_world");
        addProperty("storage.accounts.columns.location_x", STRING, true, "location_x");
        addProperty("storage.accounts.columns.location_y", STRING, true, "location_y");
        addProperty("storage.accounts.columns.location_z", STRING, true, "location_z");
        addProperty("storage.accounts.columns.location_yaw", STRING, true, "location_yaw");
        addProperty("storage.accounts.columns.location_pitch", STRING, true, "location_pitch");
        addProperty("storage.accounts.columns.in_wr", STRING, true, "in_wr");
        addProperty("storage.inventories.filename", STRING, true, "inventories.db");
        addProperty("storage.sessions.filename", STRING, false, "sessions.db");
        
        addProperty("backup.path", STRING, false, "backup");
        addProperty("backup.filename-format", STRING, false, "yyyy-MM-dd_HH-mm-ss'.db'");
        addProperty("backup.schedule.enabled", BOOLEAN, false, false);
        addProperty("backup.schedule.interval", INT, false, 120);
        
        save();
    }
    
    public void save()
    {
        plugin.saveConfig();
    }
    
    public Map<String, Property> getProperties()
    {
        return properties;
    }
    
    public Property getProperty(String path)
    {
        return properties.get(path);
    }
    
    public boolean contains(String path)
    {
        return properties.containsKey(path);
    }
    
    public PropertyType getType(String path)
    {
        return properties.get(path).getType();
    }
    
    public String toString(String path)
    {
        return properties.get(path).toString();
    }
    
    public Object get(String path)
    {
        return properties.get(path);
    }
    
    public boolean getBoolean(String path)
    {
        return (Boolean) properties.get(path).getValue();
    }
    
    public Color getColor(String path)
    {
        return (Color) properties.get(path).getValue();
    }
    
    public double getDouble(String path)
    {
        return (Double) properties.get(path).getValue();
    }
    
    public int getInt(String path)
    {
        return (Integer) properties.get(path).getValue();
    }
    
    public ItemStack getItemStack(String path)
    {
        return (ItemStack) properties.get(path).getValue();
    }
    
    public long getLong(String path)
    {
        return (Long) properties.get(path).getValue();
    }
    
    public String getString(String path)
    {
        return (String) properties.get(path).getValue();
    }
    
    public Vector getVector(String path)
    {
        return (Vector) properties.get(path).getValue();
    }
    
    public List getList(String path)
    {
        return (List) properties.get(path).getValue();
    }
    
    public List<String> getStringList(String path)
    {
        return (List<String>) properties.get(path).getValue();
    }
    
    public void set(String path, Object value) throws InvalidPropertyValueException
    {
        properties.get(path).set(value);
    }
    
    @Override
    public void update(Property p)
    {
        plugin.getConfig().set(p.getPath(), p.getValue());
        plugin.saveConfig();
        
        plugin.getCore().log(Level.INFO, LogItPlugin.getMessage("CONFIG_PROPERTY_SET_LOG", new String[]{
            "%path%", p.getPath(),
            "%value%", p.toString(),
        }));
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean changeRequiresRestart,
                               Object defaultValue,
                               Object[] validValues,
                               PropertyObserver obs)
    {
        Property property = new Property(path, type, changeRequiresRestart, plugin.getConfig().get(path, defaultValue), validValues);
        
        if (obs != null)
            property.addObserver(obs);
        
        property.addObserver(this);
        
        plugin.getConfig().set(property.getPath(), property.getValue());
        properties.put(property.getPath(), property);
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean changeRequiresRestart,
                               Object defaultValue,
                               PropertyObserver obs)
    {
        addProperty(path, type, changeRequiresRestart, defaultValue, null, obs);
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean changeRequiresRestart,
                               Object defaultValue,
                               Object[] validValues)
    {
        addProperty(path, type, changeRequiresRestart, defaultValue, validValues, null);
    }
    
    protected void addProperty(String path,
                               PropertyType type,
                               boolean changeRequiresRestart,
                               Object defaultValue)
    {
        addProperty(path, type, changeRequiresRestart, defaultValue, null, null);
    }
    
    private final LogItPlugin plugin;
    private final Map<String, Property> properties = new LinkedHashMap<>();
}
