/*
 * SessionEventListener.java
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
package io.github.lucaseasedup.logit.listener;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.session.SessionEndEvent;
import io.github.lucaseasedup.logit.session.SessionStartEvent;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public final class SessionEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onStart(SessionStartEvent event)
    {
        String username = event.getUsername();
        
        Account account = getAccountManager().selectAccount(username, Arrays.asList(
                keys().username(),
                keys().persistence()
        ));
        
        if (account != null)
        {
            account.setLastActiveDate(System.currentTimeMillis() / 1000L);
        }
        
        if (!PlayerUtils.isPlayerOnline(username))
            return;
        
        final Player player = Bukkit.getPlayerExact(username);
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (getConfig("config.yml").getBoolean("groups.enabled"))
                {
                    getCore().updatePlayerGroup(player);
                }
                
                if (getCore().isPlayerForcedToLogIn(player)
                        && !getConfig("config.yml").getBoolean("messages.join.hide"))
                {
                    getMessageDispatcher().broadcastJoinMessage(player);
                }
            }
        }.runTaskLater(getPlugin(), 1L);
        
        if (getCore().isPlayerForcedToLogIn(player))
        {
            if (account != null)
            {
                getPersistenceManager().unserialize(account, player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEnd(SessionEndEvent event)
    {
        String username = event.getUsername();
        
        Account account = getAccountManager().selectAccount(username, Arrays.asList(
                keys().username(),
                keys().persistence()
        ));
        
        if (account != null)
        {
            account.setLastActiveDate(System.currentTimeMillis() / 1000L);
        }
        
        if (!PlayerUtils.isPlayerOnline(username))
            return;
        
        final Player player = Bukkit.getPlayerExact(username);
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (getConfig("config.yml").getBoolean("groups.enabled"))
                {
                    getCore().updatePlayerGroup(player);
                }
                
                if (getCore().isPlayerForcedToLogIn(player)
                        && !getConfig("config.yml").getBoolean("messages.quit.hide"))
                {
                    getMessageDispatcher().broadcastQuitMessage(player);
                }
            }
        }.runTaskLater(getPlugin(), 1L);
        
        if (getCore().isPlayerForcedToLogIn(player))
        {
            if (account != null)
            {
                getPersistenceManager().serialize(account, player);
            }
            
            if (getConfig("config.yml").getBoolean("forceLogin.periodicalPrompt.enabled"))
            {
                long promptPeriod = getConfig("config.yml")
                        .getTime("forceLogin.periodicalPrompt.period", TimeUnit.TICKS);
                
                getMessageDispatcher()
                        .dispatchRepeatingForceLoginPrompter(username, promptPeriod, promptPeriod);
            }
        }
    }
}
