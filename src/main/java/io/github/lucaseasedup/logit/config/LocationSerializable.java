/*
 * LocationSerializable.java
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

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Location")
public final class LocationSerializable implements Cloneable, ConfigurationSerializable
{
    public LocationSerializable(String world, double x, double y, double z, float yaw, float pitch)
    {
        if (world == null)
            throw new IllegalArgumentException();
        
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public LocationSerializable(String world, double x, double y, double z)
    {
        this(world, x, y, z, 0, 0);
    }
    
    @Override
    public LocationSerializable clone()
    {
        try
        {
            return (LocationSerializable) super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new Error(ex);
        }
    }
    
    @Override
    public String toString()
    {
        return "world: " + world
                + "; x: " + x
                + "; y: " + y
                + "; z: " + z
                + "; yaw: " + yaw
                + "; pitch: " + pitch;
    }
    
    public org.bukkit.Location toBukkitLocation()
    {
        return new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("world", world);
        result.put("x", x);
        result.put("y", y);
        result.put("z", z);
        result.put("yaw", yaw);
        result.put("pitch", pitch);
        
        return result;
    }
    
    public static LocationSerializable deserialize(Map<String, Object> args)
    {
        String world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        
        if (args.containsKey("world"))
        {
            world = (String) args.get("world");
        }
        
        if (args.containsKey("x"))
        {
            x = (Double) args.get("x");
        }
        
        if (args.containsKey("y"))
        {
            y = (Double) args.get("y");
        }
        
        if (args.containsKey("z"))
        {
            z = (Double) args.get("z");
        }
        
        if (args.containsKey("yaw"))
        {
            yaw = ((Double) args.get("yaw")).floatValue();
        }
        
        if (args.containsKey("pitch"))
        {
            pitch = ((Double) args.get("pitch")).floatValue();
        }
        
        return new LocationSerializable(world, x, y, z, yaw, pitch);
    }
    
    public String getWorld()
    {
        return world;
    }
    
    public double getX()
    {
        return x;
    }
    
    public double getY()
    {
        return y;
    }
    
    public double getZ()
    {
        return z;
    }
    
    public float getYaw()
    {
        return yaw;
    }
    
    public float getPitch()
    {
        return pitch;
    }
    
    public void setWorld(String world)
    {
        this.world = world;
    }
    
    public void setX(double x)
    {
        this.x = x;
    }
    
    public void setY(double y)
    {
        this.y = y;
    }
    
    public void setZ(double z)
    {
        this.z = z;
    }
    
    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }
    
    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }
    
    public static LocationSerializable fromBukkitLocation(org.bukkit.Location location)
    {
        return new LocationSerializable(location.getWorld().getName(),
                            location.getX(),
                            location.getY(),
                            location.getZ(),
                            location.getYaw(),
                            location.getPitch());
    }
    
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
}
