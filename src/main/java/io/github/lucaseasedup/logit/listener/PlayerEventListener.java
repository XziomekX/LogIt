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
import static io.github.lucaseasedup.logit.util.PlayerUtils.broadcastJoinMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.broadcastQuitMessage;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;
import io.github.lucaseasedup.logit.ForcedLoginPrompter;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
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
        boolean isRegistered = false;
        
        try
        {
            AccountKeys keys = getAccountManager().getKeys();
            List<Hashtable<String, String>> rs =
                    getAccountStorage().selectEntries(getAccountManager().getUnit(),
                            Arrays.asList(
                                    keys.username(),
                                    keys.is_locked()
                            ), new SelectorCondition(keys.username(), Infix.EQUALS, username));
            
            if (!rs.isEmpty())
            {
                isRegistered = true;
                
                if (rs.get(0).get(keys.is_locked()).equals("1"))
                {
                    event.disallow(Result.KICK_OTHER, getMessage("ACCLOCK_SUCCESS_SELF"));
                    
                    return;
                }
            }
        }
        catch (IOException ex)
        {
            log(Level.WARNING, ex);
        }
        
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
        else if (CollectionUtils.containsIgnoreCase(username,
                getConfig().getStringList("username.prohibited-usernames")))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_PROHIBITED"));
        }
        else if (isPlayerOnline(username))
        {
            event.disallow(KICK_OTHER, getMessage("USERNAME_ALREADY_USED"));
        }
        else if (getConfig().getBoolean("crowd-control.kick-unregistered") && !isRegistered)
        {
            event.disallow(KICK_OTHER, getMessage("KICK_UNREGISTERED"));
        }
        else
        {
            int freeSlots = Bukkit.getMaxPlayers() - Bukkit.getOnlinePlayers().length;
            List<String> reserveForPlayers =
                    getConfig().getStringList("crowd-control.reserve-slots.for-players");
            int reservedSlots = 0;
            
            // Calculate how many players for which slots should be reserved are online.
            for (Player p : Bukkit.getOnlinePlayers())
            {
                if (CollectionUtils.containsIgnoreCase(p.getName(), reserveForPlayers))
                {
                    reservedSlots++;
                }
            }
            
            int maxReservedSlots = getConfig().getInt("crowd-control.reserve-slots.amount");
            int unusedReservedSlots = maxReservedSlots - reservedSlots;
            int actualFreeSlots = freeSlots - unusedReservedSlots;
            
            if (actualFreeSlots <= 0
                    && !CollectionUtils.containsIgnoreCase(username, reserveForPlayers))
            {
                event.disallow(KICK_OTHER, getMessage("NO_SLOTS_FREE"));
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
        
        int validnessTime = getConfig().getInt("login-sessions.validness-time_inSecs");
        
        if (getConfig().getBoolean("login-sessions.enabled") && validnessTime > 0)
        {
            try
            {
                AccountKeys keys = getAccountManager().getKeys();
                List<Hashtable<String, String>> rs =
                    getAccountStorage().selectEntries(getAccountManager().getUnit(),
                            Arrays.asList(
                                keys.username(),
                                keys.login_session()
                            ), new SelectorCondition(keys.username(), Infix.EQUALS, username));
                
                if (!rs.isEmpty())
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
                            
                            if (ip.equals(loginIp) && currentTime - loginTime < validnessTime
                                    && !getSessionManager().isSessionAlive(username))
                            {
                                getSessionManager().startSession(username);
                            }
                        }
                    }
                }
                else if (getConfig().getBoolean("waiting-room.enabled"))
                {
                    player.teleport(getConfig().getLocation("waiting-room.location").toBukkitLocation());
                }
            }
            catch (IOException ex)
            {
                log(Level.WARNING, ex);
            }
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
            
            int promptPeriod =
                    getConfig().getInt("force-login.periodical-prompt.period_inSecs") * 20;
            int prompterId;
            
            ForcedLoginPrompter prompter = new ForcedLoginPrompter(getCore(), username);
            
            if (getConfig().getBoolean("force-login.prompt-on.join"))
            {
                if (getConfig().getBoolean("force-login.periodical-prompt.enabled"))
                {
                    prompterId = Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(),
                            prompter, 5L, promptPeriod);
                }
                else
                {
                    prompterId = Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                            prompter, 5L);
                }
                
                prompter.setTaskId(prompterId);
            }
            else if (getConfig().getBoolean("force-login.periodical-prompt.enabled"))
            {
                prompterId = Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(),
                        prompter, 5L + promptPeriod, promptPeriod);
                
                prompter.setTaskId(prompterId);
            }
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
        
        getCore().getPersistenceManager().unserialize(player);
        
        if (!getConfig().getBoolean("messages.quit.hide")
                && (!getCore().isPlayerForcedToLogIn(player) || getSessionManager().isSessionAlive(player)))
        {
            broadcastQuitMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        event.setLeaveMessage(null);
        
        getCore().getPersistenceManager().unserialize(player);
        
        if (!getConfig().getBoolean("messages.quit.hide")
                && (!getCore().isPlayerForcedToLogIn(player) || getSessionManager().isSessionAlive(player)))
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
            if (clickedBlock == null || (clickedBlock.getType() != Material.WOOD_PLATE
                    && clickedBlock.getType() != Material.STONE_PLATE))
            {
                if (getConfig().getBoolean("force-login.prompt-on.interact"))
                {
                    getCore().sendForceLoginMessage(player);
                }
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
            
            if (getConfig().getBoolean("force-login.prompt-on.interact-entity"))
            {
                getCore().sendForceLoginMessage(player);
            }
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
            
            if (getConfig().getBoolean("force-login.prompt-on.drop-item"))
            {
                getCore().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onChangedWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            getCore().sendForceLoginMessage(player);
        }
    }
}
