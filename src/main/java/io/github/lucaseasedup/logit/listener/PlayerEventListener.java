/*
 * PlayerEventListener.java
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

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import static io.github.lucaseasedup.logit.util.CollectionUtils.containsIgnoreCase;
import static io.github.lucaseasedup.logit.util.PlayerUtils.broadcastJoinMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.broadcastQuitMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;
import io.github.lucaseasedup.logit.ForcedLoginPrompter;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

public final class PlayerEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    private void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        String username = player.getName().toLowerCase();
        
        int minUsernameLength = getConfig().getInt("username.min-length");
        int maxUsernameLength = getConfig().getInt("username.max-length");
        
        if (username.trim().isEmpty())
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_BLANK"));
        }
        else if (!player.getName().matches(getConfig().getString("username.regex")))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_INVALID"));
        }
        else if (username.length() < minUsernameLength)
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_SHORT")
                    .replace("%min-length%", String.valueOf(minUsernameLength)));
        }
        else if (username.length() > maxUsernameLength)
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_TOO_LONG")
                    .replace("%max-length%", String.valueOf(maxUsernameLength)));
        }
        else if (getConfig().getStringList("username.prohibited-usernames").contains(username))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_PROHIBITED"));
        }
        else if (isPlayerOnline(username))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_ALREADY_USED"));
        }
        else if (getConfig().getBoolean("crowd-control.kick-unregistered")
                && !getAccountManager().isRegistered(username))
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
                if (containsIgnoreCase(p.getName(), preserveForPlayers))
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
    
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent event)
    {
        final Player player   = event.getPlayer();
        final String username = player.getName().toLowerCase();
        final String ip       = getPlayerIp(player);
        
        event.setJoinMessage(null);
        
        if (getSessionManager().getSession(username) == null)
        {
            getSessionManager().createSession(username, ip);
        }
        
        int rememberLoginFor = getConfig().getInt("login-sessions.valid-for");
        
        try
        {
            AccountKeys keys = getAccountManager().getKeys();
            List<Hashtable<String, String>> rs =
                    getAccountManager().getStorage().selectEntries(getAccountManager().getUnit(),
                            Arrays.asList(
                                keys.username(),
                                keys.login_session()
                            ), new SelectorCondition(keys.username(), Infix.EQUALS, username));
            
            if (rememberLoginFor > 0 && !rs.isEmpty()
                    && getConfig().getBoolean("login-sessions.enabled"))
            {
                String loginSession = rs.get(0).get(keys.login_session());
                
                if (loginSession != null && !loginSession.isEmpty())
                {
                    String[] loginSplit = loginSession.split(";");
                    
                    if (loginSplit.length == 2)
                    {
                        String loginIp = loginSplit[0];
                        int loginTime = Integer.parseInt(loginSplit[1]);
                        int currentTime = (int) (System.currentTimeMillis() / 1000L);
                        
                        if (ip.equals(loginIp) && currentTime - loginTime < rememberLoginFor
                                && !getSessionManager().isSessionAlive(username))
                        {
                            getSessionManager().startSession(username);
                        }
                    }
                }
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
        }
        
        if (getSessionManager().isSessionAlive(player) || !getCore().isPlayerForcedToLogIn(player))
        {
            if (!getConfig().getBoolean("messages.join.hide"))
            {
                broadcastJoinMessage(player, getConfig().getBoolean("messages.join.show-world"));
            }
        }
        else
        {
            getCore().getPersistenceManager().serialize(player);
            
            int promptPeriod = getConfig().getInt("force-login.prompt-period") * 20;
            int prompterId;
            
            ForcedLoginPrompter prompter = new ForcedLoginPrompter(getCore(), username);
            
            if (promptPeriod <= 0)
            {
                prompterId = Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                        prompter, 5L);
            }
            else
            {
                prompterId = Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(),
                        prompter, 5L, promptPeriod);
            }
            
            prompter.setTaskId(prompterId);
        }
        
        if (getConfig().getBoolean("groups.enabled"))
        {
            getCore().updatePlayerGroup(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        
        event.setQuitMessage(null);
        
        if (!getCore().getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            getCore().getPersistenceManager().unserialize(player);
        }
        else if (!getConfig().getBoolean("messages.quit.hide"))
        {
            broadcastQuitMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        event.setLeaveMessage(null);
        
        if (!getCore().getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            getCore().getPersistenceManager().unserialize(player);
        }
        else if (!getConfig().getBoolean("messages.quit.hide"))
        {
            broadcastQuitMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onMove(PlayerMoveEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.move"))
            return;
        
        Player player = event.getPlayer();

        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setTo(event.getFrom());
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onToggleSneak(PlayerToggleSneakEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.toggle-sneak"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onChat(AsyncPlayerChatEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.chat"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
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
            if (message.equalsIgnoreCase("/" + alias)
                    || message.toLowerCase().startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        // Check if the sent command starts with any of "/register" aliases.
        for (String alias : registerAliases)
        {
            if (message.equalsIgnoreCase("/" + alias)
                    || message.toLowerCase().startsWith("/" + alias + " "))
            {
                return;
            }
        }
        
        // Check if the sent command is one of the allowed in the config.
        for (String command : getConfig().getStringList("force-login.allowed-commands"))
        {
            if (message.equalsIgnoreCase("/" + command)
                    || message.toLowerCase().startsWith("/" + command + " "))
            {
                return;
            }
        }
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onInteract(PlayerInteractEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.interact"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            Block clickedBlock = event.getClickedBlock();
            
            // Check if not on a pressure plate to prevent spamming.
            if (clickedBlock == null
                    || (clickedBlock.getTypeId() != 70 && clickedBlock.getTypeId() != 72))
            {
                getCore().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onInteractEntity(PlayerInteractEntityEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.interact-entity"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPickupItem(PlayerPickupItemEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.pickup-item"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onDropItem(PlayerDropItemEvent event)
    {
        if (!getConfig().getBoolean("force-login.prevent.drop-item"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getCore().sendForceLoginMessage(player);
        }
    }
}
