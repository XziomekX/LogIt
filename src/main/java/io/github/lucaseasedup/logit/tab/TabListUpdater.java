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
package io.github.lucaseasedup.logit.tab;

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.common.Wrapper;
import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.craftreflect.CraftPlayer;
import io.github.lucaseasedup.logit.craftreflect.CraftReflect;
import io.github.lucaseasedup.logit.craftreflect.EntityPlayer;
import io.github.lucaseasedup.logit.session.SessionStartEvent;
import io.github.lucaseasedup.logit.tabapi.TabAPI;
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
    public TabListUpdater(Wrapper<TabAPI> tabApiWrapper, CraftReflect craftReflect)
    {
        if (tabApiWrapper == null)
            throw new IllegalArgumentException();
        
        this.tabApiWrapper = tabApiWrapper;
        this.craftReflect = craftReflect;
    }
    
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
        
        if (getTabApi() == null)
            return;
        
        getTabApi().clearTab(player);
        
        int horizSize = getTabApi().getHorizSize();
        int vertSize = getTabApi().getVertSize();
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
            
            if (craftReflect == null)
            {
                ping = 0;
            }
            else
            {
                CraftPlayer craftPlayer = craftReflect.getCraftPlayer(p);
                EntityPlayer entityPlayer = craftPlayer.getHandle();
                
                ping = entityPlayer.getPing();
            }
            
            getTabApi().setTabString(player, j, i, p.getPlayerListName(), ping);
            
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
        
        getTabApi().updatePlayer(player);
        getTabApi().setPriority(player, 1);
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
    
    private TabAPI getTabApi()
    {
        return tabApiWrapper.get();
    }
    
    /**
     * Recommended task period of {@code TabListUpdater} running as a Bukkit task.
     */
    public static final long TASK_PERIOD = TimeUnit.SECONDS.convert(2, TimeUnit.TICKS);
    
    private Wrapper<TabAPI> tabApiWrapper;
    private final CraftReflect craftReflect;
}
