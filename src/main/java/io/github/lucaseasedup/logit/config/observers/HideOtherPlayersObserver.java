package io.github.lucaseasedup.logit.config.observers;

import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyObserver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class HideOtherPlayersObserver extends PropertyObserver
{
    @Override
    public void update(Property p)
    {
        if (p.getBoolean())
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (getSessionManager().isSessionAlive(player)
                        || !getCore().isPlayerForcedToLogIn(player))
                {
                    continue;
                }
                
                for (Player otherPlayer : Bukkit.getOnlinePlayers())
                {
                    if (otherPlayer == player)
                        continue;
                    
                    otherPlayer.hidePlayer(player);
                    player.hidePlayer(otherPlayer);
                }
            }
        }
        else
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (getSessionManager().isSessionAlive(player)
                        || !getCore().isPlayerForcedToLogIn(player))
                {
                    continue;
                }
                
                for (Player otherPlayer : Bukkit.getOnlinePlayers())
                {
                    if (otherPlayer == player)
                        continue;
                    
                    otherPlayer.showPlayer(player);
                    player.showPlayer(otherPlayer);
                }
            }
        }
    }
}
