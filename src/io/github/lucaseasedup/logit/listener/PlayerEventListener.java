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
package io.github.lucaseasedup.logit.listener;

import io.github.lucaseasedup.logit.LogItCore;
import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.MessageSender.*;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;
import static org.bukkit.event.player.PlayerLoginEvent.Result.*;
import org.bukkit.event.player.*;

/**
 * @author LucasEasedUp
 */
public class PlayerEventListener extends EventListener
{
    public PlayerEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler(priority = LOWEST)
    private void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        
        if (player.getName().trim().isEmpty())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_BLANK"));
        }
        else if (!player.getName().matches(core.getConfig().getString("username.regex")))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_INVALID"));
        }
        else if (player.getName().length() < core.getConfig().getInt("username.min-length"))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_SHORT").replace("%min-length%",
                    String.valueOf(core.getConfig().getInt("username.min-length"))));
        }
        else if (player.getName().length() > core.getConfig().getInt("username.max-length"))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_LONG").replace("%max-length%",
                    String.valueOf(core.getConfig().getInt("username.max-length"))));
        }
        else if (core.getConfig().getStringList("username.prohibited-usernames").contains(player.getName().toLowerCase()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_PROHIBITED"));
        }
        else if (isPlayerOnline(player.getName()))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_ALREADY_USED"));
        }
        else if (!core.getAccountManager().isRegistered(player.getName()) && core.getConfig().getBoolean("kick-unregistered"))
        {
            event.disallow(KICK_OTHER, getMessage("KICK_UNREGISTERED"));
        }
        else
        {
            int freeSlots = Bukkit.getMaxPlayers() - Bukkit.getOnlinePlayers().length;
            List<String> preserveSlotsPlayers = core.getConfig().getStringList("preserve-slots.players");
            
            int preservedSlots = 0;
            boolean preservedForThisPlayer = false;
            
            // Calculate how many players for which slots should be preserved are online.
            for (Player p : Bukkit.getOnlinePlayers())
            {
                if (preserveSlotsPlayers.contains(p.getName()))
                {
                    preservedSlots++;
                }
            }
            
            // Determine if the player currently trying to log in can occupy preserved slots.
            for (String name : preserveSlotsPlayers)
            {
                if (name.equalsIgnoreCase(player.getName()))
                {
                    preservedForThisPlayer = true;
                }
            }
            
            if (freeSlots - (core.getConfig().getInt("preserve-slots.amount") - preservedSlots) <= 0 && !preservedForThisPlayer)
            {
                event.disallow(KICK_FULL, getMessage("NO_SLOTS_FREE"));
            }
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onJoin(PlayerJoinEvent event)
    {
        final Player player   = event.getPlayer();
        final String username = player.getName();
        final String ip       = getPlayerIp(player);
        
        event.setJoinMessage(null);
        
        if (core.getSessionManager().getSession(username) == null)
            core.getSessionManager().createSession(username, ip);
        
        if (core.getConfig().getBoolean("groups.enabled"))
            core.updatePlayerGroup(player);
        
        if (core.isPlayerForcedToLogin(player) && !core.getSessionManager().isSessionAlive(username)
                && core.getConfig().getBoolean("force-login.hide-inventory"))
        {
            core.getInventoryDepository().deposit(player);
        }
        
        if ((core.getSessionManager().isSessionAlive(player) && core.getSessionManager().getSession(username).getIp().equals(ip))
                        || !core.getConfig().getBoolean("force-login.global") || player.hasPermission("logit.force-login.exempt"))
        {
            broadcastJoinMessage(player, core.getConfig().getBoolean("reveal-spawn-world"));
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(core.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                if (core.getConfig().getBoolean("waiting-room.enabled"))
                {
                    core.getWaitingRoom().put(player);
                }
                else if (core.getWaitingRoom().contains(player))
                {
                    core.getWaitingRoom().remove(player);
                }
                
                if (core.isPlayerForcedToLogin(player) && !core.getSessionManager().isSessionAlive(username))
                {
                    sendForceLoginMessage(player, core.getAccountManager());
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
            broadcastQuitMessage(player);
        
        core.getInventoryDepository().withdraw(player);
        
        if (core.getConfig().getBoolean("waiting-room.enabled"))
        {
            core.getWaitingRoom().put(player);
        }
    }
    
    @EventHandler
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        event.setLeaveMessage(null);
        
        core.getInventoryDepository().withdraw(player);
        
        if (core.getConfig().getBoolean("waiting-room.enabled"))
        {
            core.getWaitingRoom().put(player);
        }
    }
    
    @EventHandler
    private void onMove(PlayerMoveEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.move"))
            return;
        
        Player player = event.getPlayer();

        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setTo(event.getFrom());
        }
    }
    
    @EventHandler
    private void onToggleSneak(PlayerToggleSneakEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.toggle-sneak"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onChat(AsyncPlayerChatEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.chat"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            sendForceLoginMessage(player, core.getAccountManager());
        }
    }
    
    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.command-preprocess"))
            return;
        
        String       message = event.getMessage();
        List<String> loginAliases = core.getPlugin().getCommand("login").getAliases();
        List<String> registerAliases = core.getPlugin().getCommand("register").getAliases();
        
        // Check if the sent command starts with "/login" or "/register".
        if (message.startsWith("/login ") || message.startsWith("/register "))
            return;
        
        // Check if the sent command starts with any of "/login" aliases.
        for (String alias : loginAliases)
        {
            if (message.startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        // Check if the sent command starts with any of "/register" aliases.
        for (String alias : registerAliases)
        {
            if (message.startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        // Check if the sent command is one of the allowed in the config.
        for (String command : core.getConfig().getStringList("force-login.allowed-commands"))
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
            sendForceLoginMessage(player, core.getAccountManager());
        }
    }
    
    @EventHandler
    private void onInteract(PlayerInteractEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.interact"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            
            Block clickedBlock = event.getClickedBlock();
            
            // Check if not on a pressure plate to prevent spamming.
            if (clickedBlock == null || (clickedBlock.getTypeId() != 70 && clickedBlock.getTypeId() != 72))
            {
                sendForceLoginMessage(player, core.getAccountManager());
            }
        }
    }
    
    @EventHandler
    private void onInteractEntity(PlayerInteractEntityEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.interact-entity"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            sendForceLoginMessage(player, core.getAccountManager());
        }
    }
    
    @EventHandler
    private void onPickupItem(PlayerPickupItemEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.pickup-item"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onDropItem(PlayerDropItemEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.drop-item"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            sendForceLoginMessage(player, core.getAccountManager());
        }
    }
}
