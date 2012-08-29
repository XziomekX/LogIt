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
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.*;
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
    
    @EventHandler(priority = LOWEST)
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
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_SHORT").replace("%min-length%",
                    String.valueOf(core.getConfig().getUsernameMinLength())));
        }
        else if (player.getName().length() > core.getConfig().getUsernameMaxLength())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_LONG").replace("%max-length%",
                    String.valueOf(core.getConfig().getUsernameMaxLength())));
        }
        else if (core.getConfig().getUsernameProhibitedUsernames().contains(player.getName().toLowerCase()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_PROHIBITED"));
        }
        else if (isPlayerOnline(player.getName()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_ALREADY_USED"));
        }
        else if (!core.getAccountManager().isAccountCreated(player.getName()) && core.getConfig().getKickUnregistered())
        {
            event.disallow(KICK_OTHER, getMessage("KICK_UNREGISTERED"));
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        
        event.setJoinMessage(null);
        
        if (core.getSessionManager().getSession(player) == null)
        {
            core.getSessionManager().createSession(player);
        }
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(core.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (core.getSessionManager().isSessionAlive(player) || !core.getConfig().getForceLoginGlobal()
                        || player.hasPermission("logit.login.exempt"))
                {
                    String spawnWorldInfo = SpawnWorldInfoGenerator.getInstance().generate(player);
                    
                    for (Player p : Bukkit.getServer().getOnlinePlayers())
                    {
                        if (!p.equals(player))
                        {
                            p.sendMessage(getMessage("JOIN").replace("%player%", player.getName()) + spawnWorldInfo);
                        }
                    }
                }
                else if (core.getConfig().getForceLoginGlobal() && core.getConfig().getWaitingRoomEnabled())
                {
                    core.getWaitingRoom().put(player);
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
            broadcastMessage(getMessage("QUIT").replace("%player%", player.getName()));
        }
        
        core.getWaitingRoom().remove(player);
    }
    
    @EventHandler
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        event.setLeaveMessage(null);
        
        core.getWaitingRoom().remove(player);
    }
    
    @EventHandler
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        
        if (!core.getConfig().getForceLoginPreventMove())
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
        
        if (!core.getConfig().getForceLoginPreventToggleSneak())
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
        
        if (!core.getConfig().getForceLoginPreventChat())
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
        if (!core.getConfig().getForceLoginPreventCommandPreprocess())
            return;
        
        String       message = event.getMessage();
        List<String> loginAliases = core.getPlugin().getCommand("login").getAliases();
        List<String> registerAliases = core.getPlugin().getCommand("register").getAliases();
        
        if (message.startsWith("/login ") || message.startsWith("/register "))
        {
            return;
        }
        
        for (String alias : loginAliases)
        {
            if (message.startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        for (String alias : registerAliases)
        {
            if (message.startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        for (String command : core.getConfig().getForceLoginAllowedCommands())
        {
            if (message.equalsIgnoreCase("/" + command) || message.startsWith("/" + command + " "))
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
        
        if (!core.getConfig().getForceLoginPreventInteract())
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
        
        if (!core.getConfig().getForceLoginPreventInteractEntity())
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
        
        if (!core.getConfig().getForceLoginPreventPickupItem())
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
        
        if (!core.getConfig().getForceLoginPreventDropItem())
            return;
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            core.sendForceLoginMessage(player);
        }
    }
    
    private final LogItCore core;
}
