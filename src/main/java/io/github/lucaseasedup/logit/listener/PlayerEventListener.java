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

import static io.github.lucaseasedup.logit.util.MessageHelper._;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import static io.github.lucaseasedup.logit.util.PlayerUtils.isPlayerOnline;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.account.AccountKeys;
import io.github.lucaseasedup.logit.config.LocationSerializable;
import io.github.lucaseasedup.logit.session.Session;
import io.github.lucaseasedup.logit.storage.Storage;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import io.github.lucaseasedup.logit.util.JoinMessageGenerator;
import io.github.lucaseasedup.logit.util.QuitMessageGenerator;
import java.util.Arrays;
import java.util.List;
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
    @EventHandler(priority = EventPriority.NORMAL)
    private void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        String username = player.getName().toLowerCase();
        
        AccountKeys keys = getAccountManager().getKeys();
        Storage.Entry accountData = getAccountManager().queryAccount(username,
                Arrays.asList(keys.username(), keys.is_locked()));
        
        if (accountData != null)
        {
            if ("1".equals(accountData.get(keys.is_locked())))
            {
                event.disallow(Result.KICK_OTHER, _("ACCLOCK_SUCCESS_SELF"));
                
                return;
            }
        }
        
        int minUsernameLength = getConfig().getInt("username.min-length");
        int maxUsernameLength = getConfig().getInt("username.max-length");
        
        if (username.trim().isEmpty())
        {
            event.disallow(KICK_OTHER, _("usernameBlank"));
        }
        else if (!player.getName().matches(getConfig().getString("username.regex")))
        {
            event.disallow(KICK_OTHER, _("usernameInvalid"));
        }
        else if (username.length() < minUsernameLength)
        {
            event.disallow(KICK_OTHER, _("usernameTooShort")
                    .replace("{0}", String.valueOf(minUsernameLength)));
        }
        else if (username.length() > maxUsernameLength)
        {
            event.disallow(KICK_OTHER, _("usernameTooLong")
                    .replace("{0}", String.valueOf(maxUsernameLength)));
        }
        else if (CollectionUtils.containsIgnoreCase(username,
                getConfig().getStringList("username.prohibited-usernames")))
        {
            event.disallow(KICK_OTHER, _("usernameProhibited"));
        }
        else if (isPlayerOnline(username))
        {
            event.disallow(KICK_OTHER, _("usernameAlreadyUsed"));
        }
        else if (getConfig().getBoolean("crowd-control.kick-unregistered") && accountData == null)
        {
            event.disallow(KICK_OTHER, _("kickUnregistered"));
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
                event.disallow(KICK_OTHER, _("noSlotsFree"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
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
        
        long validnessTime = getConfig().getTime("login-sessions.validness-time", TimeUnit.SECONDS);
        
        if (getConfig().getBoolean("login-sessions.enabled") && validnessTime > 0)
        {
            AccountKeys keys = getAccountManager().getKeys();
            Storage.Entry accountData = getAccountManager().queryAccount(username,
                    Arrays.asList(keys.username(), keys.login_session()));
            
            if (accountData != null)
            {
                String loginSession = accountData.get(keys.login_session());
                
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
                LocationSerializable waitingRoomLocationSerializable =
                        getConfig().getLocation("waiting-room.location");
                
                player.teleport(waitingRoomLocationSerializable.toBukkitLocation());
            }
        }
        
        if (getSessionManager().isSessionAlive(player) || !getCore().isPlayerForcedToLogIn(player))
        {
            if (!getConfig().getBoolean("messages.join.hide"))
            {
                event.setJoinMessage(JoinMessageGenerator.generate(player,
                        getConfig().getBoolean("messages.join.show-world")));
            }
        }
        else
        {
            getCore().getPersistenceManager().serialize(player);
            
            long promptPeriod =
                    getConfig().getTime("force-login.periodical-prompt.period", TimeUnit.TICKS);
            
            if (getConfig().getBoolean("force-login.prompt-on.join"))
            {
                if (getConfig().getBoolean("force-login.periodical-prompt.enabled"))
                {
                    getMessageDispatcher().dispatchRepeatingForceLoginPrompter(username,
                            TimeUnit.SECONDS.convert(1, TimeUnit.TICKS), promptPeriod);
                }
                else
                {
                    getMessageDispatcher().dispatchForceLoginPrompter(username, 5L);
                }
            }
            else if (getConfig().getBoolean("force-login.periodical-prompt.enabled"))
            {
                getMessageDispatcher().dispatchRepeatingForceLoginPrompter(username,
                        TimeUnit.SECONDS.convert(1, TimeUnit.TICKS) + promptPeriod, promptPeriod);
            }
        }
        
        if (getConfig().getBoolean("groups.enabled"))
        {
            getCore().updatePlayerGroup(player);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    private void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        
        getCore().getPersistenceManager().unserialize(player);
        
        if (!getConfig().getBoolean("messages.quit.hide")
                && (!getCore().isPlayerForcedToLogIn(player)
                        || getSessionManager().isSessionAlive(player)))
        {
            event.setQuitMessage(QuitMessageGenerator.generate(player));
        }
        else
        {
            event.setQuitMessage(null);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onKick(PlayerKickEvent event)
    {
        Player player = event.getPlayer();
        
        getCore().getPersistenceManager().unserialize(player);
        
        if (!getConfig().getBoolean("messages.quit.hide")
                && (!getCore().isPlayerForcedToLogIn(player)
                        || getSessionManager().isSessionAlive(player)))
        {
            event.setLeaveMessage(QuitMessageGenerator.generate(player));
        }
        else
        {
            event.setLeaveMessage(null);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig().getBoolean("force-login.prevent.move")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setTo(event.getFrom());
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onToggleSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig().getBoolean("force-login.prevent.toggle-sneak")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onChat(AsyncPlayerChatEvent event)
    {
        if (!isCoreStarted())
            return;
        
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig().getBoolean("force-login.prevent.chat")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getMessageDispatcher().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (!isCoreStarted())
            return;
        
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
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
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getMessageDispatcher().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig().getBoolean("force-login.prevent.interact")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            Block clickedBlock = event.getClickedBlock();
            
            // Check if not on a pressure plate to prevent spamming.
            if (clickedBlock == null || (clickedBlock.getType() != Material.WOOD_PLATE
                    && clickedBlock.getType() != Material.STONE_PLATE))
            {
                if (getConfig().getBoolean("force-login.prompt-on.interact"))
                {
                    getMessageDispatcher().sendForceLoginMessage(player);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onInteractEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig().getBoolean("force-login.prevent.interact-entity")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig().getBoolean("force-login.prompt-on.interact-entity"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player.getName());
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig().getBoolean("force-login.prevent.drop-item")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig().getBoolean("force-login.prompt-on.drop-item"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onChangedWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player) && getCore().isPlayerForcedToLogIn(player))
        {
            getMessageDispatcher().sendForceLoginMessage(player);
        }
    }
}
