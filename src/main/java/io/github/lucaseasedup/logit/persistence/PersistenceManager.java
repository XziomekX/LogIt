/*
 * PersistenceManager.java
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
package io.github.lucaseasedup.logit.persistence;

import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/**
 * Provides a centred persistence serialization interface.
 */
public final class PersistenceManager extends LogItCoreObject implements Disposable
{
    public PersistenceManager()
    {
        serializers = new HashMap<>();
    }
    
    @Override
    public void dispose()
    {
        if (serializers != null)
        {
            serializers.clear();
            serializers = null;
        }
    }
    
    /**
     * Serializes player data using the specified persistence serializer
     * 
     * <p> No action will be taken if the player's data
     * has already been serialized using this serializer.
     * 
     * @param player the player whose data will be serialized.
     * @param clazz  serializer class.
     * 
     * @throws IllegalArgumentException     if {@code player} or {@code clazz} is {@code null}.
     * @throws ReflectiveOperationException if serializer construction failed.
     * @throws IOException                  if an I/O error occured while updating the persistence.
     */
    public void serializeUsing(Player player, Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException, IOException
    {
        if (player == null || clazz == null)
            throw new IllegalArgumentException();
        
        String username = player.getName().toLowerCase();
        PersistenceSerializer serializer = getSerializer(clazz);
        Map<String, String> persistence = getAccountManager().getAccountPersistence(username);
        
        if (persistence == null)
            return;
        
        if (!isSerializedUsing(persistence, serializer))
        {
            serializer.serialize(persistence, player);
            
            getAccountManager().updateAccountPersistence(username, persistence);
        }
    }
    
    /**
     * Serializes player data using all enabled serializers registered
     * using {@link #registerSerializer} method.
     * 
     * @param player the player whose data will be serialized.
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public void serialize(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        String username = player.getName().toLowerCase();
        
        try
        {
            Map<String, String> persistence = getAccountManager().getAccountPersistence(username);
            
            if (persistence == null)
                return;
            
            for (Class<? extends PersistenceSerializer> clazz : getSerializersInOrder())
            {
                try
                {
                    PersistenceSerializer serializer = getSerializer(clazz);
                    
                    if (!isSerializedUsing(persistence, serializer))
                    {
                        serializer.serialize(persistence, player);
                    }
                }
                catch (ReflectiveOperationException ex)
                {
                    log(Level.WARNING,
                            "Could not obtain persistence serializer: " + clazz.getName(), ex);
                }
            }
            
            getAccountManager().updateAccountPersistence(username, persistence);
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    "Could not serialize persistence for player: " + player.getName(), ex);
        }
    }

    /**
     * Unserializes player data using the specified persistence serializer
     * 
     * <p> No action will be taken if the player's data
     * has not been serialized using this serializer.
     * 
     * @param player the player whose data will be unserialized.
     * @param clazz  serializer class.
     * 
     * @throws IllegalArgumentException     if {@code player} or {@code clazz} is {@code null}.
     * @throws ReflectiveOperationException if serializer construction failed.
     * @throws IOException                  if an I/O error occured while updating the persistence.
     */
    public void unserializeUsing(Player player, Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException, IOException
    {
        if (player == null || clazz == null)
            throw new IllegalArgumentException();
        
        String username = player.getName().toLowerCase();
        PersistenceSerializer serializer = getSerializer(clazz);
        Map<String, String> persistence = getAccountManager().getAccountPersistence(username);
        
        if (persistence == null)
            return;
        
        if (isSerializedUsing(persistence, serializer))
        {
            serializer.unserialize(persistence, player);
            
            for (Key key : getSerializerKeys(serializer.getClass()))
            {
                persistence.put(key.name(), key.defaultValue());
            }
            
            getAccountManager().updateAccountPersistence(username, persistence);
        }
    }

    /**
     * Unserializes player data using all enabled serializers registered
     * using {@link #registerSerializer} method.
     * 
     * @param player the player whose data will be unserialized.
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public void unserialize(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        String username = player.getName().toLowerCase();
        Set<Key> keysToErase = new HashSet<>();
        
        try
        {
            Map<String, String> persistence = getAccountManager().getAccountPersistence(username);
            
            if (persistence == null)
                return;
            
            for (Class<? extends PersistenceSerializer> clazz : getSerializersInOrder())
            {
                PersistenceSerializer serializer;
                
                try
                {
                    serializer = getSerializer(clazz);
                    
                    if (isSerializedUsing(persistence, serializer))
                    {
                        for (Key key : getSerializerKeys(serializer.getClass()))
                        {
                            keysToErase.add(key);
                        }
                        
                        serializer.unserialize(persistence, player);
                    }
                }
                catch (ReflectiveOperationException ex)
                {
                    log(Level.WARNING,
                            "Could not obtain persistence serializer: " + clazz.getName(), ex);
                }
            }
            
            for (Key key : keysToErase)
            {
                persistence.put(key.name(), key.defaultValue());
            }
            
            getAccountManager().updateAccountPersistence(username, persistence);
        }
        catch (IOException ex)
        {
            log(Level.WARNING,
                    "Could not unserialize persistence for player: " + player.getName(), ex);
        }
    }
    
    /**
     * Registers a serializer class.
     * 
     * @param clazz the serializer class to be registered.
     * 
     * @return {@code false} if the serializer class was already registered; {@code true} otherwise.
     * 
     * @throws IllegalArgumentException     if {@code clazz} is {@code null}.
     * @throws ReflectiveOperationException if serializer constructor invocation failed.
     * 
     */
    public boolean registerSerializer(Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException
    {
        if (clazz == null)
            throw new IllegalArgumentException();
        
        if (serializers.containsKey(clazz))
            return false;
        
        serializers.put(clazz, constructSerializer(clazz));
        
        return true;
    }
    
    /**
     * Unregisters a serializer class.
     * 
     * @param clazz the serializer class to be unregistered.
     * 
     * @return {@code false} if the serializer class was not registered; {@code true} otherwise.
     * 
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     */
    public boolean unregisterSerializer(Class<? extends PersistenceSerializer> clazz)
    {
        if (clazz == null)
            throw new IllegalArgumentException();
        
        if (!serializers.containsKey(clazz))
            return false;
        
        serializers.remove(clazz);
        
        return true;
    }
    
    public PersistenceSerializer getSerializer(Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException
    {
        PersistenceSerializer serializer = serializers.get(clazz);
        
        if (serializer == null)
        {
            return constructSerializer(clazz);
        }
        
        return serializer;
    }
    
    @SuppressWarnings("incomplete-switch")
    private boolean isSerializedUsing(Map<String, String> persistence,
                                      PersistenceSerializer serializer)
    {
        for (Key key : getSerializerKeys(serializer.getClass()))
        {
            String value = persistence.get(key.name());
            
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
    
    private Class<? extends PersistenceSerializer>[] getSerializersInOrder()
    {
        Set<Class<? extends PersistenceSerializer>> classes = serializers.keySet();
        
        @SuppressWarnings("unchecked")
        Class<? extends PersistenceSerializer>[] result =
                classes.toArray(new Class[classes.size()]);
        
        Arrays.sort(result, new Comparator<Class<? extends PersistenceSerializer>>()
        {
            @Override
            public int compare(Class<? extends PersistenceSerializer> o1,
                               Class<? extends PersistenceSerializer> o2)
            {
                if (o1 == o2)
                    return 0;
                
                Before o1BeforeAnnotation = o1.getAnnotation(Before.class);
                After o1AfterAnnotation = o1.getAnnotation(After.class);
                Before o2BeforeAnnotation = o2.getAnnotation(Before.class);
                After o2AfterAnnotation = o2.getAnnotation(After.class);
                
                Class<? extends PersistenceSerializer> o1Before =
                        (o1BeforeAnnotation != null) ? o1BeforeAnnotation.value() : null;
                Class<? extends PersistenceSerializer> o1After =
                        (o1AfterAnnotation != null) ? o1AfterAnnotation.value() : null;
                Class<? extends PersistenceSerializer> o2Before =
                        (o2BeforeAnnotation != null) ? o2BeforeAnnotation.value() : null;
                Class<? extends PersistenceSerializer> o2After =
                        (o2AfterAnnotation != null) ? o2AfterAnnotation.value() : null;
                
                if (o1Before == o1After && (o1Before != null || o1After != null))
                    throw new RuntimeException();
                
                if (o2Before == o2After && (o2Before != null || o2After != null))
                    throw new RuntimeException();
                
                if (o1Before == o2Before && (o1Before != null || o2Before != null))
                    throw new RuntimeException("Circular serializer dependency.");

                if (o1After == o2After && (o1After != null || o2After != null))
                    throw new RuntimeException("Circular serializer dependency.");
                
                if (o1Before == o2)
                    return -1;
                
                if (o1After == o2)
                    return 1;
                
                if (o2Before == o1)
                    return 1;
                
                if (o2After == o1)
                    return -1;
                
                return 0;
            }
        });
        
        return result;
    }
    
    private Key[] getSerializerKeys(Class<? extends PersistenceSerializer> clazz)
    {
        if (clazz == null)
            return NO_KEYS;
        
        Keys keys = clazz.getAnnotation(Keys.class);
        
        if (keys == null)
            return NO_KEYS;
        
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
    
    private static PersistenceSerializer constructSerializer(Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException
    {
        return clazz.getConstructor().newInstance();
    }
    
    private static final Key[] NO_KEYS = new Key[0];
    
    private Map<Class<? extends PersistenceSerializer>, PersistenceSerializer> serializers;
}
