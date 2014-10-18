package io.github.lucaseasedup.logit.persistence;

import java.util.Map;
import org.bukkit.entity.Player;

@Keys({
    @Key(name = "air", constraint = KeyConstraint.NOT_EMPTY),
})
public final class AirBarSerializer implements PersistenceSerializer
{
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
                player.setRemainingAir(Integer.parseInt(air));
            }
        }
    }
}
