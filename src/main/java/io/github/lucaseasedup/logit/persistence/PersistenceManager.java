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

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/**
 * Provides a centred persistence serialization interface.
 */
public final class PersistenceManager extends LogItCoreObject
{
    public PersistenceManager()
    {
        serializers = new HashMap<>();
    }
    
    /**
     * Serializes player data using the specified serializer
     * 
     * <p> It does nothing if {@code clazz} is {@code null}, or the player has already
     * been serialized using this serializer.
     * 
     * @param player the player whose data will be serialized.
     * @param clazz  serializer class.
     * 
     * @throws NullPointerException         if {@code player} is null.
     * @throws ReflectiveOperationException if serializer construction failed.
     * @throws IOException                  if an IO error occured while updating persistence.
     */
    public void serializeUsing(Player player, Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException, IOException
    {
        if (player == null)
            throw new NullPointerException();
        
        PersistenceSerializer serializer = getSerializer(clazz);
        
        if (isSerializedUsing(player, clazz))
            return;
        
        Map<String, String> data = new HashMap<>();
        
        serializer.serialize(data, player);
        
        for (Entry<String, String> entry : data.entrySet())
        {
            if (containsKey(serializer.getClass(), entry.getKey()))
            {
                getAccountManager().updateAccountPersistence(player.getName(),
                        entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Serializes player data using all enabled serializers registered
     * using {@link #registerSerializer} method.
     * 
     * @param player the player whose data will be serialized.
     * 
     * @throws NullPointerException if {@code player} is null.
     */
    public void serialize(Player player)
    {
        if (player == null)
            throw new NullPointerException();
        
        for (Class<? extends PersistenceSerializer> clazz : getSerializersInOrder())
        {
            try
            {
                serializeUsing(player, clazz);
            }
            catch (ReflectiveOperationException | IOException ex)
            {
                log(Level.WARNING,
                        "Could not serialize persistence for player: " + player.getName(), ex);
            }
        }
    }

    /**
     * Unserializes player data using the specified serializer
     * 
     * <p> It does nothing if {@code clazz} is {@code null}, or the player has not
     * been serialized using this serializer.
     * 
     * @param player the player whose data will be unserialized.
     * @param clazz  serializer class.
     * 
     * @throws NullPointerException         if {@code player} is null.
     * @throws ReflectiveOperationException if serializer construction failed.
     * @throws IOException                  if an IO error occured while updating persistence.
     */
    public void unserializeUsing(Player player, Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException, IOException
    {
        if (player == null)
            throw new NullPointerException();
        
        PersistenceSerializer serializer = getSerializer(clazz);
        
        if (!isSerializedUsing(player, clazz))
            return;
        
        Map<String, String> data = new HashMap<>();
        
        for (Key key : getSerializerKeys(serializer.getClass()))
        {
            data.put(key.name(),
                    getAccountManager().getAccountPersistence(player.getName(), key.name()));
            
            // Erase persistence.
            getAccountManager().updateAccountPersistence(player.getName(),
                    key.name(), key.defaultValue());
        }
        
        serializer.unserialize(data, player);
    }

    /**
     * Unserializes player data using all enabled serializers registered
     * using {@link #registerSerializer} method.
     * 
     * @param player the player whose data will be unserialized.
     * 
     * @throws NullPointerException if {@code player} is null.
     */
    public void unserialize(Player player)
    {
        if (player == null)
            throw new NullPointerException();
        
        for (Class<? extends PersistenceSerializer> clazz : getSerializersInOrder())
        {
            try
            {
                unserializeUsing(player, clazz);
            }
            catch (ReflectiveOperationException | IOException ex)
            {
                log(Level.WARNING,
                        "Could not unserialize persistence for player: " + player.getName(), ex);
            }
        }
    }
    
    /**
     * Registers a serializer class.
     * 
     * @param clazz serializer class.
     * 
     * @return {@code false} if the serializer class is already registered; {@code true} otherwise.
     * 
     * @throws NullPointerException         if {@code clazz} is null.
     * @throws ReflectiveOperationException if serializer constructor invocation failed.
     * 
     */
    public boolean registerSerializer(Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException
    {
        if (clazz == null)
            throw new NullPointerException();
        
        if (serializers.containsKey(clazz))
            return false;
        
        serializers.put(clazz, constructSerializer(clazz));
        
        return true;
    }
    
    /**
     * Unregisters a serializer class.
     * 
     * @param clazz serializer class to be unregistered.
     * 
     * @return {@code false} if the serializer class is not registered; {@code true} otherwise.
     * 
     * @throws NullPointerException if {@code clazz} is null.
     */
    public boolean unregisterSerializer(Class<? extends PersistenceSerializer> clazz)
    {
        if (clazz == null)
            throw new NullPointerException();
        
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
    private boolean isSerializedUsing(Player player, Class<? extends PersistenceSerializer> clazz)
            throws IOException
    {
        PersistenceSerializer serializer = serializers.get(clazz);
        
        if (player == null)
            return false;
        
        if (serializer == null)
        {
            try
            {
                serializer = constructSerializer(clazz);
            }
            catch (ReflectiveOperationException ex)
            {
                return false;
            }
        }
        
        for (Key key : getSerializerKeys(serializer.getClass()))
        {
            String value = getAccountManager().getAccountPersistence(player.getName(), key.name());
            
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
    
    private final Map<Class<? extends PersistenceSerializer>, PersistenceSerializer> serializers;
}
