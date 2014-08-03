/*
 * QueuedMap.java
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
package io.github.lucaseasedup.logit.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public final class QueuedMap<K, V> implements Map<K, V>
{
    @Override
    public synchronized V put(K key, V value)
    {
        if (key == null)
            throw new IllegalArgumentException();
        
        if (keys.contains(key))
        {
            return values.put(key, value);
        }
        else
        {
            enqueue(key, value);
            
            return null;
        }
    }
    
    @Override
    public synchronized V get(Object key)
    {
        if (key == null)
            throw new IllegalArgumentException();
        
        return values.get(key);
    }
    
    @Override
    public synchronized V remove(Object key)
    {
        if (key == null)
            throw new IllegalArgumentException();
        
        keys.remove(key);
        
        return values.remove(key);
    }
    
    public synchronized Map.Entry<K, V> remove()
    {
        final K key = keys.remove();
        final V value = values.remove(key);
        
        return new Map.Entry<K, V>()
        {
            @Override
            public K getKey()
            {
                return key;
            }
            
            @Override
            public V getValue()
            {
                return value;
            }
            
            @Override
            public V setValue(V value)
            {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public synchronized int size()
    {
        return keys.size();
    }
    
    @Override
    public synchronized boolean isEmpty()
    {
        return size() == 0;
    }
    
    @Override
    public synchronized boolean containsKey(Object key)
    {
        return keys.contains(key);
    }
    
    @Override
    public synchronized boolean containsValue(Object value)
    {
        return values.containsValue(value);
    }
    
    @Override
    public Set<K> keySet()
    {
        Set<K> keySet = new TreeSet<>();
        
        keySet.addAll(keys);
        
        return keySet;
    }
    
    @Override
    public Collection<V> values()
    {
        return values.values();
    }
    
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        return values.entrySet();
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> map)
    {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }
    
    @Override
    public synchronized void clear()
    {
        keys.clear();
        values.clear();
    }
    
    private void enqueue(K key, V value)
    {
        if (key == null)
            throw new IllegalArgumentException();
        
        if (keys.add(key))
        {
            values.put(key, value);
        }
    }
    
    private final Queue<K> keys = new LinkedList<>();
    private final Map<K, V> values = new HashMap<>();
}
