package io.github.lucaseasedup.logit.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerCollections implements Listener
{
    private PlayerCollections()
    {
        validateMaps();
        validateCollections();
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        
        for (Map<Player, ?> map : maps)
        {
            map.remove(player);
        }
        
        for (Collection<Player> collection : collections)
        {
            collection.remove(player);
        }
    }
    
    private void validateMaps()
    {
        for (Map<Player, ?> map : maps)
        {
            Iterator<Player> it = map.keySet().iterator();
            
            while (it.hasNext())
            {
                Player player = it.next();
                
                if (!player.isOnline())
                {
                    it.remove();
                }
            }
        }
    }
    
    private void validateCollections()
    {
        for (Collection<Player> collection : collections)
        {
            Iterator<Player> it = collection.iterator();
            
            while (it.hasNext())
            {
                Player player = it.next();
                
                if (!player.isOnline())
                {
                    it.remove();
                }
            }
        }
    }
    
    public static synchronized <T extends Map<Player, U>, U> T monitoredMap(T map)
    {
        if (map == null)
            throw new IllegalArgumentException();
        
        maps.add(map);
        
        return map;
    }
    
    public static synchronized
            <T extends Collection<Player>> T monitoredCollection(T collection)
    {
        if (collection == null)
            throw new IllegalArgumentException();
        
        collections.add(collection);
        
        return collection;
    }
    
    public static synchronized void registerListener(JavaPlugin plugin)
    {
        if (plugin == null)
            throw new IllegalArgumentException();
        
        Bukkit.getPluginManager().registerEvents(new PlayerCollections(), plugin);
    }
    
    private static final Set<Map<Player, ?>> maps = new HashSet<>();
    private static final Set<Collection<Player>> collections = new HashSet<>();
}
