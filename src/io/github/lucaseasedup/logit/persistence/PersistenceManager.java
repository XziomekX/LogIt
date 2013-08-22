/*
 * PersistenceManager.java
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
package io.github.lucaseasedup.logit.persistence;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.ReportedException;
import io.github.lucaseasedup.logit.account.Account;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/**
 * Provides a centred persistence serialization interface.
 * 
 * @author LucasEasedUp
 */
public final class PersistenceManager extends LogItCoreObject
{
    public PersistenceManager(LogItCore core)
    {
        super(core);
        
        serializers = new HashMap<>();
    }
    
    /**
     * Checks if a serializer is enabled.
     * 
     * <p> This method looks for the following annotations in this order
     * (if found, all other are ignored):<ol>
     *  <li>{@link EnabledStatic} set to {@code true},</li>
     *  <li><code>{@link EnabledCheckerClass}.value().newInstance().isEnabled</code>
     *      returning {@code true},</li>
     *  <li>{@link EnabledConfigProperty} pointing to a config property set to <i>true</i>.</li>
     * </ol>
     * 
     * @param clazz serializer class.
     * @return {@code true} is the serializer is enabled; {@code false} otherwise.
     */
    public boolean isSerializerEnabled(Class<? extends PersistenceSerializer> clazz)
    {
        if (!serializers.containsKey(clazz))
            return false;
        
        EnabledStatic enabledStatic = clazz.getAnnotation(EnabledStatic.class);
        
        if (enabledStatic != null)
        {
            return enabledStatic.value();
        }
        
        EnabledCheckerClass enabledCheckerClass =
                clazz.getAnnotation(EnabledCheckerClass.class);
        
        if (enabledCheckerClass != null)
        {
            try
            {
                return enabledCheckerClass.value().newInstance().isEnabled();
            }
            catch (InstantiationException | IllegalAccessException ex)
            {
            }
        }
        
        EnabledConfigProperty enabledConfigProperty =
                clazz.getAnnotation(EnabledConfigProperty.class);
        
        if (enabledConfigProperty != null)
        {
            return getConfig().getBoolean(enabledConfigProperty.value());
        }
        
        return false;
    }
    
    /**
     * Serializes player data using the specified serializer
     * 
     * <p> It does nothing if {@code clazz} is {@code null}, the player has already
     * been serialized using this serializer, or the serializer has not been registered
     * using {@link #registerSerializer} method.
     * 
     * @param player player whose data will be serialized.
     * @param clazz serializer class.
     */
    public void serializeUsing(Player player, Class<? extends PersistenceSerializer> clazz)
    {
        Account account = getAccountManager().getAccount(player.getName());
        PersistenceSerializer serializer = serializers.get(clazz);
        
        if (serializer == null)
            return;
        
        if (isSerializedUsing(player, clazz))
            return;
        
        try
        {
            Map<String, String> data = new HashMap<>();
            
            serializer.serialize(data, player);
            
            if (account != null)
            {
                for (Entry<String, String> entry : data.entrySet())
                {
                    if (containsKey(serializer.getClass(), entry.getKey()))
                    {
                        account.updatePersistence(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not serialize player persistence using "
                               + serializer.getClass().getSimpleName(), ex);
            
            ReportedException.throwNew(ex);
        }
    }
    
    /**
     * Serializes player data using all enabled serializers registered
     * using {@link #registerSerializer} method.
     * 
     * @param player player whose data will be serialized.
     */
    public void serialize(Player player)
    {
        for (Class<? extends PersistenceSerializer> clazz : serializers.keySet())
        {
            if (!isSerializerEnabled(clazz))
                return;
            
            serializeUsing(player, clazz);
        }
    }

    /**
     * Unserializes player data using the specified serializer
     * 
     * <p> It does nothing if {@code clazz} is {@code null}, the player has not
     * been serialized using this serializer, or the serializer has not been registered
     * using {@link #registerSerializer} method.
     * 
     * @param player player whose data will be unserialized.
     * @param clazz serializer class.
     */
    public void unserializeUsing(Player player, Class<? extends PersistenceSerializer> clazz)
    {
        Account account = getAccountManager().getAccount(player.getName());
        PersistenceSerializer serializer = serializers.get(clazz);
        
        if (serializer == null)
            return;
        
        if (!isSerializedUsing(player, clazz))
            return;
        
        try
        {
            Map<String, String> data = new HashMap<>();
            
            for (Key key : getSerializerKeys(serializer.getClass()))
            {
                data.put(key.name(), account.getPersistence(key.name()));
                
                // Erase persistence.
                account.updatePersistence(key.name(), key.defaultValue());
            }
            
            serializer.unserialize(data, player);
        }
        catch (SQLException ex)
        {
            log(Level.WARNING, "Could not unserialize player persistence using "
                               + serializer.getClass().getSimpleName(), ex);
            
            ReportedException.throwNew(ex);
        }
    }

    /**
     * Unserializes player data using all enabled serializers registered
     * using {@link #registerSerializer} method.
     * 
     * @param player player whose data will be unserialized.
     */
    public void unserialize(Player player)
    {
        for (Class<? extends PersistenceSerializer> clazz : serializers.keySet())
        {
            if (!isSerializerEnabled(clazz))
                return;
            
            unserializeUsing(player, clazz);
        }
    }
    
    /**
     * Registers a serializer class.
     * 
     * @param clazz serializer class.
     * @throws ReflectiveOperationException if serializer constructor invocation failed.
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     */
    public void registerSerializer(Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException
    {
        if (clazz == null)
            throw new IllegalArgumentException("Serializer class must not be null.");
        
        if (serializers.containsKey(clazz))
            throw new RuntimeException(clazz.getSimpleName() + " is already registered.");
        
        serializers.put(clazz, clazz.getConstructor(LogItCore.class).newInstance(getCore()));
    }
    
    @SuppressWarnings("incomplete-switch")
    private boolean isSerializedUsing(Player player, Class<? extends PersistenceSerializer> clazz)
    {
        PersistenceSerializer serializer = serializers.get(clazz);
        
        if (serializer == null || player == null)
            return false;
        
        Account account = getAccountManager().getAccount(player.getName());
        
        for (Key key : getSerializerKeys(serializer.getClass()))
        {
            String value = account.getPersistence(key.name());
            
            switch (key.constraint())
            {
            case NON_NULL:
            {
                if (value == null)
                {
                    return false;
                }
                
                break;
            }
            case NOT_EMPTY:
            {
                if (value == null || value.isEmpty())
                {
                    return false;
                }
                
                break;
            }
            }
        }
        
        return true;
    }
    
    private Key[] getSerializerKeys(Class<? extends PersistenceSerializer> clazz)
    {
        if (clazz == null || !serializers.containsKey(clazz))
            return new Key[0];
        
        Keys keys = clazz.getAnnotation(Keys.class);
        
        if (keys == null)
            return new Key[0];
        
        return keys.value();
    }
    
    private boolean containsKey(Class<? extends PersistenceSerializer> clazz, String keyName)
    {
        Key[] keys = getSerializerKeys(clazz);
        
        for (Key key : keys)
        {
            if (key.name().equals(keyName))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private final Map<Class<? extends PersistenceSerializer>, PersistenceSerializer> serializers;
}
