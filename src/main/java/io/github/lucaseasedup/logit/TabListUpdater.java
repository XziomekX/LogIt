/*
 * TabListUpdater.java
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

import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.craftreflect.CraftPlayer;
import io.github.lucaseasedup.logit.craftreflect.EntityPlayer;
import io.github.lucaseasedup.logit.session.SessionStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class TabListUpdater extends LogItCoreObject implements Runnable, Listener
{
    @Override
    public void run()
    {
        updateAllTabLists();
    }
    
    public void updateAllTabLists()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            updateTabList(player);
        }
    }
    
    private void updateTabList(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (getCore().getTabApi() == null)
            return;
        
        getCore().getTabApi().clearTab(player);
        
        int horizSize = getCore().getTabApi().getHorizSize();
        int vertSize = getCore().getTabApi().getVertSize();
        int i = 0;
        int j = 0;
        
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (!getSessionManager().isSessionAlive(p) && !p.equals(player)
                    && getConfig("config.yml").getBoolean("forceLogin.hideFromTabList"))
            {
                continue;
            }
            
            int ping;
            
            if (getCore().getCraftReflect() == null)
            {
                ping = 0;
            }
            else
            {
                CraftPlayer craftPlayer = getCore().getCraftReflect().getCraftPlayer(p);
                EntityPlayer entityPlayer = craftPlayer.getHandle();
                
                ping = entityPlayer.getPing();
            }
            
            getCore().getTabApi().setTabString(player, j, i, p.getPlayerListName(), ping);
            
            i++;
            
            if (i >= horizSize)
            {
                i = 0;
                j++;
            }
            
            if (j >= vertSize)
            {
                break;
            }
        }
        
        getCore().getTabApi().updatePlayer(player);
        getCore().getTabApi().setPriority(player, 1);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                getCore().getTabListUpdater().updateAllTabLists();
            }
        }.runTaskLater(getPlugin(), 1L);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                getCore().getTabListUpdater().updateAllTabLists();
            }
        }.runTaskLater(getPlugin(), 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSessionStart(SessionStartEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                getCore().getTabListUpdater().updateAllTabLists();
            }
        }.runTaskLater(getPlugin(), 1L);
    }
    
    /**
     * Recommended task period of {@code TabListUpdater} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = TimeUnit.SECONDS.convert(2, TimeUnit.TICKS);
}
