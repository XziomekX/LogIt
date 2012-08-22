/*
 * PlayerEventListener.java
 *
 * Copyright (C) 2012 LucasEasedUp
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
package com.gmail.lucaseasedup.logit.event.listener;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.LogItPlugin.*;
import com.gmail.lucaseasedup.logit.SpawnWorldInfoGenerator;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.HIGHEST;
import org.bukkit.event.Listener;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;
import org.bukkit.event.player.*;

/**
 * @author LucasEasedUp
 */
public class PlayerEventListener implements Listener
{
    public PlayerEventListener(LogItCore core)
    {
        this.core = core;
    }
    
    @EventHandler
    private void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        
        if (player.getName().trim().isEmpty())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_BLANK"));
        }
        else if (!player.getName().matches(core.getConfig().getUsernameRegex()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_INVALID"));
        }
        else if (player.getName().length() < core.getConfig().getUsernameMinLength())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_SHORT").replace("%min-length%", String.valueOf(core.getConfig().getUsernameMinLength())));
        }
        else if (player.getName().length() > core.getConfig().getUsernameMaxLength())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_LONG").replace("%max-length%", String.valueOf(core.getConfig().getUsernameMaxLength())));
        }
        else if (core.getConfig().getUsernameProhibitedUsernames().contains(player.getName().toLowerCase()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_PROHIBITED"));
        }
        else if (isPlayerOnline(player.getName()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_ALREADY_USED"));
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        
        event.setJoinMessage(null);
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(core.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (core.getSessionManager().isSessionAlive(player) || !core.getConfig().getForceLogin() || player.hasPermission("logit.login.exempt"))
                {
                    broadcastMessage(getMessage("JOIN").replace("%player%", player.getName()) + SpawnWorldInfoGenerator.getInstance().generate(player));
                }
                else
                {
                    core.getSessionManager().createSession(player);
                    
                    if (core.getConfig().getForceLogin() && core.getConfig().getWaitingRoomEnabled())
                    {
                        core.putIntoWaitingRoom(player);
                    }
                }
                
                if (core.isPlayerForcedToLogin(player) && !core.getSessionManager().isSessionAlive(player))
                {
                    core.sendForceLoginMessage(player);
                }
            }
        }, 1L);
    }
    
    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        
        event.setQuitMessage(null);
        
        if (core.getSessionManager().isSessionAlive(player))
        {
            broadcastMessage(getMessage("QUIT").replace("%player%", player.getName()) + SpawnWorldInfoGenerator.getInstance().generate(player));
        }
        
        core.takeOutOfWaitingRoom(player);
    }
    
    @EventHandler
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        event.setLeaveMessage(null);
        
        core.takeOutOfWaitingRoom(player);
    }
    
    @EventHandler
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionMove())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setTo(event.getFrom());
        }
    }
    
    @EventHandler
    private void onToggleSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionToggleSneak())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionChat())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendForceLoginMessage(player);
        }
    }
    
    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (!core.getConfig().getOutOfSessionEventPreventionCommandPreprocess())
            return;
        
        for (String command : core.getConfig().getOutOfSessionAllowedCommands())
        {
            if (event.getMessage().equalsIgnoreCase("/" + command) || event.getMessage().startsWith("/" + command + " "))
            {
                return;
            }
        }
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendForceLoginMessage(player);
        }
    }
    
    @EventHandler
    private void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionInteract())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            
            // Check if not on a pressure plate to prevent spamming.
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null || (clickedBlock.getTypeId() != 70 && clickedBlock.getTypeId() != 72))
            {
                core.sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler
    private void onInteractEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionInteractEntity())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendForceLoginMessage(player);
        }
    }
    
    @EventHandler
    private void onPickupItem(PlayerPickupItemEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionPickupItem())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getOutOfSessionEventPreventionDropItem())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendForceLoginMessage(player);
        }
    }
    
    private LogItCore core;
}
