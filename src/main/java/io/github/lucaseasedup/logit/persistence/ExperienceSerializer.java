package io.github.lucaseasedup.logit.persistence;

import java.util.Map;
import org.bukkit.entity.Player;

@Keys({
    @Key(name = "exp", constraint = KeyConstraint.NOT_EMPTY),
    @Key(name = "level", constraint = KeyConstraint.NOT_EMPTY),
})
public final class ExperienceSerializer implements PersistenceSerializer
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
