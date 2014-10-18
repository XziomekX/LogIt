package io.github.lucaseasedup.logit.persistence;

import java.util.Map;
import org.bukkit.entity.Player;

@Keys({
    @Key(name = "health", constraint = KeyConstraint.NOT_EMPTY),
})
public final class HealthBarSerializer implements PersistenceSerializer
{
    @Override
    public void serialize(Map<String, String> data, Player player)
    {
        String health = String.valueOf(player.getHealth());
        
        data.put("health", health);
        
        if (player.isOnline())
        {
            player.setHealth(player.getMaxHealth());
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, Player player)
    {
        String health = data.get("health");
        
        if (health != null)
        {
            if (player.isOnline())
            {
                player.setHealth(Double.valueOf(health).intValue());
            }
        }
    }
}
