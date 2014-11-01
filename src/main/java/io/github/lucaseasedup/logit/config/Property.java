package io.github.lucaseasedup.logit.config;

import io.github.lucaseasedup.logit.common.Disposable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Observable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class Property extends Observable implements Disposable
{
    public Property(String path,
                    PropertyType type,
                    boolean requiresRestart,
                    Object value,
                    PropertyValidator validator)
    {
        if (StringUtils.isBlank(path) || type == null)
            throw new IllegalArgumentException();
        
        this.path = path;
        this.type = type;
        this.requiresRestart = requiresRestart;
        this.value = value;
        this.validator = validator;
    }
    
    @Override
    public void dispose()
    {
        deleteObservers();
        
        path = null;
        type = null;
        value = null;
        validator = null;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public PropertyType getType()
    {
        return type;
    }
    
    public boolean requiresRestart()
    {
        return requiresRestart;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[").append(type.toString()).append("] ");
        
        switch (type)
        {
        case CONFIGURATION_SECTION:
        case OBJECT:
        case BOOLEAN:
        case COLOR:
        case INT:
        case ITEM_STACK:
        case LONG:
        case STRING:
        case VECTOR:
        case LOCATION:
            sb.append(value.toString());
            break;
            
        case DOUBLE:
            sb.append(new BigDecimal(getDouble()).setScale(2));
            break;
            
        case LIST:
        case BOOLEAN_LIST:
        case BYTE_LIST:
        case CHARACTER_LIST:
        case DOUBLE_LIST:
        case FLOAT_LIST:
        case INTEGER_LIST:
        case LONG_LIST:
        case MAP_LIST:
        case SHORT_LIST:
        case STRING_LIST:
            sb.append(StringUtils.join((List<?>) value, ", "));
            break;
            
        default:
            throw new RuntimeException("Unknown property type: " + type);
        }
        
        return sb.toString();
    }
    
    public Object getValue()
    {
        return value;
    }
    
    public String getStringifiedValue()
    {
        switch (type)
        {
        case DOUBLE:
            return "~" + new BigDecimal(getDouble()).setScale(2, RoundingMode.HALF_UP);
            
        default:
            return value.toString();
        }
    }
    
    public boolean getBoolean()
    {
        return (Boolean) value;
    }
    
    public Color getColor()
    {
        return (Color) value;
    }
    
    public double getDouble()
    {
        return (Double) value;
    }
    
    public int getInt()
    {
        return (Integer) value;
    }
    
    public ItemStack getItemStack()
    {
        return (ItemStack) value;
    }
    
    public long getLong()
    {
        return (Long) value;
    }
    
    public String getString()
    {
        return (String) value;
    }
    
    public Vector getVector()
    {
        return (Vector) value;
    }
    
    public List<?> getList()
    {
        return (List<?>) value;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getStringList()
    {
        return (List<String>) value;
    }
    
    public LocationSerializable getLocation()
    {
        return (LocationSerializable) value;
    }
    
    public void set(Object value) throws InvalidPropertyValueException
    {
        setSilently(value);
        setChanged();
        notifyObservers();
    }
    
    /* package */ void setSilently(Object value) throws InvalidPropertyValueException
    {
        if (validator != null && !validator.validate(path, type, value))
            throw new InvalidPropertyValueException(path);
        
        this.value = value;
    }
    
    private String path;
    private PropertyType type;
    private final boolean requiresRestart;
    private Object value;
    private PropertyValidator validator;
}
