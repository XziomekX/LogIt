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
            cooldowns = null;
        }
    }
    
    public boolean isCooldownActive(Player player, Cooldown cooldown)
    {
        if (player == null || cooldown == null)
            throw new IllegalArgumentException();
        
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
        if (player == null || cooldown == null)
            throw new IllegalArgumentException();
        
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
        if (player == null || cooldown == null || cooldownMillis < 0)
            throw new IllegalArgumentException();
        
        if (!cooldowns.containsKey(player))
        {
            cooldowns.put(player, new HashMap<Cooldown, Long>());
        }
        
        cooldowns.get(player).put(cooldown, System.currentTimeMillis() + cooldownMillis);
    }
    
    public void deactivateCooldown(Player player, Cooldown cooldown)
    {
        if (player == null || cooldown == null)
            throw new IllegalArgumentException();
        
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
