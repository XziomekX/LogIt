/*
 * SerializerObserver.java
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
package io.github.lucaseasedup.logit.config.observers;

import io.github.lucaseasedup.logit.PlayerHolder;
import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyObserver;
import io.github.lucaseasedup.logit.persistence.AirBarSerializer;
import io.github.lucaseasedup.logit.persistence.ExperienceSerializer;
import io.github.lucaseasedup.logit.persistence.HealthBarSerializer;
import io.github.lucaseasedup.logit.persistence.HungerBarSerializer;
import io.github.lucaseasedup.logit.persistence.InventorySerializer;
import io.github.lucaseasedup.logit.persistence.LocationSerializer;
import io.github.lucaseasedup.logit.persistence.PersistenceSerializer;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
public final class SerializerObserver extends PropertyObserver
{
    @Override
    public void update(Property p)
    {
        Class<? extends PersistenceSerializer> clazz = null;
        
        switch (p.getPath().toLowerCase())
        {
        case "waiting-room.enabled":
            clazz = LocationSerializer.class;
            break;
            
        case "force-login.obfuscate-bars.air":
            clazz = AirBarSerializer.class;
            break;

        case "force-login.obfuscate-bars.health":
            clazz = HealthBarSerializer.class;
            break;
            
        case "force-login.obfuscate-bars.experience":
            clazz = ExperienceSerializer.class;
            break;

        case "force-login.obfuscate-bars.hunger":
            clazz = HungerBarSerializer.class;
            break;
            
        case "force-login.hide-inventory":
            clazz = InventorySerializer.class;
            break;
        }
        
        if (clazz != null)
        {
            update(clazz, p.getBoolean());
        }
    }
    
    private void update(Class<? extends PersistenceSerializer> clazz, boolean status)
    {
        if (status)
        {
            for (Player player : PlayerHolder.getAll())
            {
                try
                {
                    getPersistenceManager().serializeUsing(player, clazz);
                }
                catch (ReflectiveOperationException ex)
                {
                }
            }
            
            try
            {
                getPersistenceManager().registerSerializer(clazz);
            }
            catch (ReflectiveOperationException ex)
            {
                log(Level.WARNING, "Could not register serializer: " + clazz.getSimpleName(), ex);
            }
        }
        else
        {
            for (Player player : PlayerHolder.getAll())
            {
                try
                {
                    getPersistenceManager().unserializeUsing(player, clazz);
                }
                catch (ReflectiveOperationException ex)
                {
                }
            }
            
            getPersistenceManager().unregisterSerializer(clazz);
        }
    }
}
