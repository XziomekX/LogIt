package io.github.lucaseasedup.logit.config;

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
