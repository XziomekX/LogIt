package io.github.lucaseasedup.logit.persistence;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.common.ReportedException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

/**
 * Provides a centralized persistence serialization platform.
 */
public final class PersistenceManager extends LogItCoreObject
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
     * has already been serialized using the specified serializer.
     * 
     * @param account the account from which persistence data will be read.
     * @param player  the player whose data will be serialized.
     * @param clazz   the serializer class.
     * 
     * @throws IllegalArgumentException if {@code account}, {@code player}
     *                                  or {@code clazz} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public void serializeUsing(Account account,
                               Player player,
                               Class<? extends PersistenceSerializer> clazz)
    {
        if (account == null || player == null || clazz == null)
            throw new IllegalArgumentException();
        
        PersistenceSerializer serializer = getSerializer(clazz);
        
        if (serializer == null)
            return;
        
        serializeUsing(account, player, serializer);
    }
    
    public void serializeUsing(Account account, Player player, PersistenceSerializer serializer)
    {
        if (account == null || player == null || serializer == null)
            throw new IllegalArgumentException();
        
        Map<String, String> persistence = account.getPersistence();
        
        if (persistence == null)
            return;
        
        if (!isSerializedUsing(persistence, serializer))
        {
            serializer.serialize(persistence, player);
            
            account.savePersistence(persistence);
        }
    }
    
    /**
     * Serializes player data using all enabled serializers registered
     * using the {@link #registerSerializer} method.
     * 
     * @param account the account from which persistence data will be read.
     * @param player  the player whose data will be serialized.
     * 
     * @throws IllegalArgumentException if {@code account} or
     *                                  {@code player} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public void serialize(Account account, Player player)
    {
        if (account == null || player == null)
            throw new IllegalArgumentException();
        
        Map<String, String> persistence = account.getPersistence();
        
        if (persistence == null)
            return;
        
        for (Class<? extends PersistenceSerializer> clazz : getSerializersInOrder())
        {
            PersistenceSerializer serializer = getSerializer(clazz);
            
            if (serializer == null)
                continue;
            
            if (!isSerializedUsing(persistence, serializer))
            {
                serializer.serialize(persistence, player);
            }
        }
        
        account.savePersistence(persistence);
    }
    
    /**
     * Unserializes player data using the specified persistence serializer
     * 
     * <p> No action will be taken if the player's data
     * has not been serialized using the specified serializer.
     * 
     * @param account the account from which persistence data will be read.
     * @param player  the player whose data will be unserialized.
     * @param clazz   the serializer class.
     * 
     * @throws IllegalArgumentException if {@code account}, {@code player} or
     *                                  {@code clazz} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public void unserializeUsing(Account account,
                                 Player player,
                                 Class<? extends PersistenceSerializer> clazz)
    {
        if (account == null || player == null || clazz == null)
            throw new IllegalArgumentException();
        
        PersistenceSerializer serializer = getSerializer(clazz);
        
        if (serializer == null)
            return;
        
        unserializeUsing(account, player, serializer);
    }
    
    public void unserializeUsing(Account account, Player player, PersistenceSerializer serializer)
    {
        if (account == null || player == null || serializer == null)
            throw new IllegalArgumentException();
        
        Map<String, String> persistence = account.getPersistence();
        
        if (persistence == null)
            return;
        
        if (isSerializedUsing(persistence, serializer))
        {
            serializer.unserialize(persistence, player);
            
            for (Key key : getSerializerKeys(serializer.getClass()))
            {
                persistence.put(key.name(), key.defaultValue());
            }
            
            account.savePersistence(persistence);
        }
    }
    
    /**
     * Unserializes player data using all enabled serializers registered
     * using the {@link #registerSerializer} method.
     * 
     * @param account the account from which persistence data will be read.
     * @param player  the player whose data will be unserialized.
     * 
     * @throws IllegalArgumentException if {@code account} or
     *                                  {@code player} is {@code null}.
     * 
     * @throws ReportedException        if an I/O error occurred,
     *                                  and it was reported to the logger.
     */
    public void unserialize(Account account, Player player)
    {
        if (account == null || player == null)
            throw new IllegalArgumentException();
        
        Map<String, String> persistence = account.getPersistence();
        
        if (persistence == null)
            return;
        
        Set<Key> keysToErase = new HashSet<>();
        
        for (Class<? extends PersistenceSerializer> clazz : getSerializersInOrder())
        {
            PersistenceSerializer serializer = getSerializer(clazz);
            
            if (serializer == null)
                continue;
            
            if (isSerializedUsing(persistence, serializer))
            {
                for (Key key : getSerializerKeys(serializer.getClass()))
                {
                    keysToErase.add(key);
                }
                
                serializer.unserialize(persistence, player);
            }
        }
        
        for (Key key : keysToErase)
        {
            persistence.put(key.name(), key.defaultValue());
        }
        
        account.savePersistence(persistence);
    }
    
    /**
     * Registers a serializer class.
     * 
     * @param clazz the serializer class to be registered.
     * 
     * @return {@code false} if the serializer class was already registered;
     *         {@code true} otherwise.
     * 
     * @throws ReflectiveOperationException if serializer constructor invocation failed.
     * @throws IllegalArgumentException     if {@code clazz} is {@code null}.
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
     * @return {@code false} if the serializer class was not registered;
     *         {@code true} otherwise.
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
    
    /**
     * Returns an instance of a serializer based on the provided class.
     * 
     * @param clazz the serializer class.
     * 
     * @return the serializer instance, or {@code null}
     *         if this serializer class has not been
     *         registered in this {@code PersistenceManager}.
     * 
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     */
    public PersistenceSerializer getSerializer(Class<? extends PersistenceSerializer> clazz)
    {
        if (clazz == null)
            throw new IllegalArgumentException();
        
        return serializers.get(clazz);
    }
    
    @SuppressWarnings("incomplete-switch")
    private boolean isSerializedUsing(Map<String, String> persistence,
                                      PersistenceSerializer serializer)
    {
        if (persistence == null || serializer == null)
            throw new IllegalArgumentException();
        
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
            case NOT_BLANK:
            {
                if (StringUtils.isBlank(value))
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
            throw new IllegalArgumentException();
        
        Keys keys = clazz.getAnnotation(Keys.class);
        
        if (keys == null)
            return new Key[0];
        
        return keys.value();
    }
    
    private boolean containsKey(Class<? extends PersistenceSerializer> clazz, String keyName)
    {
        if (clazz == null || keyName == null)
            throw new IllegalArgumentException();
        
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
    
    private static PersistenceSerializer
                        constructSerializer(Class<? extends PersistenceSerializer> clazz)
            throws ReflectiveOperationException
    {
        if (clazz == null)
            throw new IllegalArgumentException();
        
        return clazz.getConstructor().newInstance();
    }
    
    private Map<Class<? extends PersistenceSerializer>, PersistenceSerializer> serializers;
}
