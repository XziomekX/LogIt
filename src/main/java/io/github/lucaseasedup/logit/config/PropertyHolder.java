/*
 * PropertyHolder.java
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
package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface PropertyHolder
{
    public Map<String, Property> getProperties();
    public Property getProperty(String path);
    
    public boolean contains(String path);
    public Set<String> getKeys(String path);
    public Map<String, Object> getValues(String path);
    public Object get(String path);
    public boolean getBoolean(String path);
    public List<Boolean> getBooleanList(String path);
    public List<Byte> getByteList(String path);
    public List<Character> getCharacterList(String path);
    public Color getColor(String path);
    public double getDouble(String path);
    public List<Double> getDoubleList(String path);
    public List<Float> getFloatList(String path);
    public int getInt(String path);
    public List<Integer> getIntegerList(String path);
    public ItemStack getItemStack(String path);
    public List<?> getList(String path);
    public long getLong(String path);
    public List<Long> getLongList(String path);
    public List<Map<?, ?>> getMapList(String path);
    public List<Short> getShortList(String path);
    public String getString(String path);
    public List<String> getStringList(String path);
    public Vector getVector(String path);
    public LocationSerializable getLocation(String path);
    public long getTime(String path, TimeUnit convertTo);
    public void set(String path, Object value) throws InvalidPropertyValueException;
}
