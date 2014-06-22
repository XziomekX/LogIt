/*
 * CooldownManager.java
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
package io.github.lucaseasedup.logit.cooldown;

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CooldownManager extends LogItCoreObject implements Listener
{
    @Override
    public void dispose()
    {
        if (cooldowns != null)
        {
            for (Map<Cooldown, Long> cooldown : cooldowns.values())
            {
                cooldown.clear();
            }
            
            cooldowns.clear();
        }
    }
    
    public boolean isCooldownActive(Player player, Cooldown cooldown)
    {
        Map<Cooldown, Long> playerCooldowns = cooldowns.get(player);
        
        if (playerCooldowns == null)
            return false;
        
        Long cooldownExpirationMillis = playerCooldowns.get(cooldown);
        
        if (cooldownExpirationMillis == null)
            return false;
        
        if (cooldownExpirationMillis > System.currentTimeMillis())
        {
            return true;
        }
        else
        {
            playerCooldowns.remove(cooldown);
            
            return false;
        }
    }
    
    /**
     * Returns actual cooldown time of the specific type, that affects a player.
     * 
     * @param player   the player who is affected by the cooldown.
     * @param cooldown the cooldown type.
     * 
     * @return the cooldown in milliseconds.
     */
    public long getCooldownMillis(Player player, Cooldown cooldown)
    {
        Map<Cooldown, Long> playerCooldowns = cooldowns.get(player);
        
        if (playerCooldowns == null)
            return -1;
        
        Long cooldownExpirationMillis = playerCooldowns.get(cooldown);
        
        if (cooldownExpirationMillis == null)
            return -1;
        
        return cooldownExpirationMillis - System.currentTimeMillis();
    }
    
    public void activateCooldown(Player player, Cooldown cooldown, long cooldownMillis)
    {
        if (!cooldowns.containsKey(player))
        {
            cooldowns.put(player, new HashMap<Cooldown, Long>());
        }
        
        cooldowns.get(player).put(cooldown, System.currentTimeMillis() + cooldownMillis);
    }
    
    public void deactivateCooldown(Player player, Cooldown cooldown)
    {
        if (!cooldowns.containsKey(player))
            return;
        
        cooldowns.get(player).remove(cooldown);
    }
    
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        cooldowns.remove(event.getPlayer());
    }
    
    private Map<Player, Map<Cooldown, Long>> cooldowns = new HashMap<>();
}
