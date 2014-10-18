package io.github.lucaseasedup.logit.persistence;

import java.util.Map;
import org.bukkit.entity.Player;

@Keys({
    @Key(name = "hunger", constraint = KeyConstraint.NOT_EMPTY),
})
public final class HungerBarSerializer implements PersistenceSerializer
{
    @Override
    public void serialize(Map<String, String> data, Player player)
    {
        String hunger = String.valueOf(player.getFoodLevel());
        
        data.put("hunger", hunger);
        
        if (player.isOnline())
        {
            player.setFoodLevel(20);
        }
    }
    
    @Override
    public void unserialize(Map<String, String> data, Player player)
    {
        if (!player.isOnline())
            return;
        
        String hunger = data.get("hunger");
        
        if (hunger != null)
        {
            player.setFoodLevel(Integer.parseInt(hunger));
        }
    }
}
