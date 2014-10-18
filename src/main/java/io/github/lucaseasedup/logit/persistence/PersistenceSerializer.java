package io.github.lucaseasedup.logit.persistence;

import java.util.Map;
import org.bukkit.entity.Player;

public interface PersistenceSerializer
{
    public void serialize(Map<String, String> data, Player player);
    public void unserialize(Map<String, String> data, Player player);
}
