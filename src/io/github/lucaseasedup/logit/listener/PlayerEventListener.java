/*
 * PlayerEventListener.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.MessageUtils.broadcastJoinMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;
import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * @author LucasEasedUp
 */
public class PlayerEventListener extends LogItCoreObject implements Listener
{
    public PlayerEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler(priority = LOWEST)
    private void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        String username = player.getName().toLowerCase();
        
        if (username.trim().isEmpty())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_BLANK"));
        }
        else if (!player.getName().matches(getConfig().getString("username.regex")))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_INVALID"));
        }
        else if (username.length() < getConfig().getInt("username.min-length"))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_SHORT").replace("%min-length%",
                    String.valueOf(getConfig().getInt("username.min-length"))));
        }
        else if (username.length() > getConfig().getInt("username.max-length"))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_LONG").replace("%max-length%",
                    String.valueOf(getConfig().getInt("username.max-length"))));
        }
        else if (getConfig().getStringList("username.prohibited-usernames").contains(username))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_PROHIBITED"));
        }
        else if (isPlayerOnline(username))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_ALREADY_USED"));
        }
        else if (!getAccountManager().isRegistered(username)
                && getConfig().getBoolean("crowd-control.kick-unregistered"))
        {
            event.disallow(KICK_OTHER, getMessage("KICK_UNREGISTERED"));
        }
        else
        {
            int freeSlots = Bukkit.getMaxPlayers() - Bukkit.getOnlinePlayers().length;
            List<String> preserveForPlayers =
                    getConfig().getStringList("crowd-control.preserve-slots.players");
            
            int preservedSlots = 0;
            boolean preservedForThisPlayer = false;
            
            // Calculate how many players for which slots should be preserved are online.
            for (Player p : Bukkit.getOnlinePlayers())
            {
                if (preserveForPlayers.contains(p.getName()))
                {
                    preservedSlots++;
                }
            }
            
            // Determine if the player currently trying to log in can occupy preserved slots.
            for (String name : preserveForPlayers)
            {
                if (name.equalsIgnoreCase(username))
                {
                    preservedForThisPlayer = true;
                }
            }
            
            int maxPreservedSlots = getConfig().getInt("crowd-control.preserve-slots.amount");
            int unusedPreservedSlots = maxPreservedSlots - preservedSlots;
            int actualFreeSlots = freeSlots - unusedPreservedSlots;
            
            if (actualFreeSlots <= 0 && !preservedForThisPlayer)
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
        
        if (getSessionManager().getSession(username) == null)
        {
            getSessionManager().createSession(username, ip);
        }
        
        if (getSessionManager().isSessionAlive(player) || !getCore().isPlayerForcedToLogin(player))
        {
            broadcastJoinMessage(player, getConfig().getBoolean("reveal-spawn-world"));
        }
        else
        {
            getCore().getPersistenceManager().serialize(player);
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
            {
                @Override
                public void run()
                {
                    if (getCore().isPlayerForcedToLogin(player) && !getSessionManager().isSessionAlive(player))
                    {
                        getCore().sendForceLoginMessage(player);
                    }
                }
            }, 5L);
        }
        
        if (getConfig().getBoolean("groups.enabled"))
        {
            getCore().updatePlayerGroup(player);
        }
    }
    
    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        
        event.setQuitMessage(null);
        
        if (getCore().getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogin(player))
        {
            getCore().getPersistenceManager().unserialize(player);
        }
    }
    
    @EventHandler
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        event.setLeaveMessage(null);
        
        if (getCore().getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogin(player))
        {
            getCore().getPersistenceManager().unserialize(player);
        }
    }
    
    @EventHandler
    private void onMove(PlayerMoveEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.move"))
            return;
        
        Player player = event.getPlayer();

        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setTo(event.getFrom());
        }
    }
    
    @EventHandler
    private void onToggleSneak(PlayerToggleSneakEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.toggle-sneak"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onChat(AsyncPlayerChatEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.chat"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.command-preprocess"))
            return;
        
        String       message = event.getMessage();
        List<String> loginAliases = getPlugin().getCommand("login").getAliases();
        List<String> registerAliases = getPlugin().getCommand("register").getAliases();
        
        // Check if the sent command starts with "/login" or "/register".
        if (message.startsWith("/login ") || message.equals("/login")
                || message.startsWith("/register ") || message.equals("/register"))
        {
            return;
        }
        
        // Check if the sent command starts with any of "/login" aliases.
        for (String alias : loginAliases)
        {
            if (message.equalsIgnoreCase("/" + alias) || message.toLowerCase().startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        // Check if the sent command starts with any of "/register" aliases.
        for (String alias : registerAliases)
        {
            if (message.equalsIgnoreCase("/" + alias) || message.toLowerCase().startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        // Check if the sent command is one of the allowed in the config.
        for (String command : getConfig().getStringList("force-login.allowed-commands"))
        {
            if (message.equalsIgnoreCase("/" + command) || message.toLowerCase().startsWith("/" + command + " "))
            {
                return;
            }
        }
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler
    private void onInteract(PlayerInteractEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.interact"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            
            Block clickedBlock = event.getClickedBlock();
            
            // Check if not on a pressure plate to prevent spamming.
            if (clickedBlock == null || (clickedBlock.getTypeId() != 70 && clickedBlock.getTypeId() != 72))
            {
                getCore().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler
    private void onInteractEntity(PlayerInteractEntityEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.interact-entity"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler
    private void onPickupItem(PlayerPickupItemEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.pickup-item"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onDropItem(PlayerDropItemEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.drop-item"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
}
