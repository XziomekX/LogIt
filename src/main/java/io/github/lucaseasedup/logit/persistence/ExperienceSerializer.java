/*
 * ExperienceSerializer.java
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

import java.util.Map;
import org.bukkit.entity.Player;

/**
 * @author LucasEasedUp
 */
@Keys({
    @Key(name = "exp", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "level", constraint = KeyConstraint.NOT_EMPTY),
})
public final class ExperienceSerializer extends PersistenceSerializer
{
    @Override
    public void serialize(Map<String, String> data, Player player)
    {
        String exp = String.valueOf(player.getExp());
        String level = String.valueOf(player.getLevel());
        
        data.put("exp", exp);
        data.put("level", level);
        
        if (player.isOnline())
        {
            player.setExp(0);
            player.setLevel(0);
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, Player player)
    {
        if (!player.isOnline())
            return;
        
        String exp = data.get("exp");
        String level = data.get("level");
        
        if (exp != null)
        {       
            player.setExp(Float.parseFloat(exp));
        }
        
        if (level != null)
        {       
            player.setLevel(Integer.parseInt(level));
        }
    }
}
