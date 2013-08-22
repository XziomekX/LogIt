/*
 * AirBarSerializer.java
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
import io.github.lucaseasedup.logit.util.MinecraftUtils;
import java.io.IOException;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
@Keys({
    @Key(name = "air", constraint = KeyConstraint.NOT_EMPTY),
})
@EnabledConfigProperty("force-login.prevent.air-depletion")
@OfflineSerializable(true)
public final class AirBarSerializer extends PersistenceSerializer
{
    public AirBarSerializer(LogItCore core)
    {
        super(core);
    }
    
    @Override
    public void serialize(Map<String, String> data, Player player)
    {
        String air = String.valueOf(player.getRemainingAir());
        
        data.put("air", air);
        
        if (player.isOnline())
        {
            player.setRemainingAir(player.getMaximumAir());
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, Player player)
    {
        String air = data.get("air");
        
        if (air != null)
        {       
            if (player.isOnline())
            {
                player.setRemainingAir(Integer.valueOf(air));
            }
            else
            {
                try
                {
                    MinecraftUtils.saveAir(player.getWorld().getName(), player.getName(), Short.valueOf(air));
                }
                catch (IOException ex)
                {
                }
            }
        }
    }
}
