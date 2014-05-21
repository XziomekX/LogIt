/*
 * LogItMessageDelegate.java
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
package io.github.lucaseasedup.logit;

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Hashtable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class LogItMessageDispatcher extends LogItCoreObject implements Listener, Disposable
{
    @Override
    public void dispose()
    {
        if (forceLoginPromptIntervals != null)
        {
            forceLoginPromptIntervals.clear();
            forceLoginPromptIntervals = null;
        }
    }
    
    /**
     * Sends a message to the given player telling them either to log in or to register.
     * 
     * <p> This method's behavior may be altered by the configuration file.
     * 
     * @param player the player to whom the message will be sent.
     */
    public void sendForceLoginMessage(Player player)
    {
        long minInterval =
                getConfig().getTime("force-login.prompt.min-interval", TimeUnit.MILLISECONDS);
        
        if (minInterval > 0)
        {
            long currentTimeMillis = System.currentTimeMillis();
            Long playerInterval = forceLoginPromptIntervals.get(player);
            
            if (playerInterval != null && currentTimeMillis - playerInterval < minInterval)
                return;
            
            forceLoginPromptIntervals.put(player, currentTimeMillis);
        }
        
        if (getAccountManager().isRegistered(player.getName()))
        {
            if (getConfig().getBoolean("force-login.prompt.login"))
            {
                if (!getConfig().getBoolean("password.disable-passwords"))
                {
                    sendMsg(player, _("PLEASE_LOGIN"));
                }
                else
                {
                    sendMsg(player, _("PLEASE_LOGIN_NOPASS"));
                }
            }
        }
        else
        {
            if (getConfig().getBoolean("force-login.prompt.register"))
            {
                if (!getConfig().getBoolean("password.disable-passwords"))
                {
                    sendMsg(player, _("PLEASE_REGISTER"));
                }
                else
                {
                    sendMsg(player, _("PLEASE_REGISTER_NOPASS"));
                }
            }
        }
    }
    
    public void dispatchForceLoginPrompter(String username, long delay)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        new ForceLoginPrompter(username).runTaskLater(getPlugin(), delay);
    }
    
    public void dispatchRepeatingForceLoginPrompter(String username, long delay, long period)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        new ForceLoginPrompter(username).runTaskTimer(getPlugin(), delay, period);
    }
    
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        forceLoginPromptIntervals.remove(event.getPlayer());
    }
    
    @EventHandler
    private void onPlayerKick(PlayerKickEvent event)
    {
        forceLoginPromptIntervals.remove(event.getPlayer());
    }
    
    private final class ForceLoginPrompter extends BukkitRunnable
    {
        public ForceLoginPrompter(String username)
        {
            this.username = username;
        }
        
        @Override
        public void run()
        {
            Player player = PlayerUtils.getPlayer(username);
            
            if (player == null || !isCoreStarted())
            {
                cancel();
            }
            else if (getCore().isPlayerForcedToLogIn(player))
            {
                if (!getSessionManager().isSessionAlive(player))
                {
                    sendForceLoginMessage(player);
                }
                else
                {
                    cancel();
                }
            }
        }
        
        private final String username;
    }
    
    private Hashtable<Player, Long> forceLoginPromptIntervals = new Hashtable<>();
}
