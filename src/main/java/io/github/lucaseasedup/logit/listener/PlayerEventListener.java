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

import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.util.MessageHelper.t;
import static io.github.lucaseasedup.logit.util.PlayerUtils.getPlayerIp;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.TimeUnit;
import io.github.lucaseasedup.logit.account.Account;
import io.github.lucaseasedup.logit.hooks.BukkitSmerfHook;
import io.github.lucaseasedup.logit.hooks.VanishNoPacketHook;
import io.github.lucaseasedup.logit.persistence.LocationSerializer;
import io.github.lucaseasedup.logit.session.Session;
import io.github.lucaseasedup.logit.storage.Infix;
import io.github.lucaseasedup.logit.storage.SelectorBinary;
import io.github.lucaseasedup.logit.storage.SelectorCondition;
import io.github.lucaseasedup.logit.storage.SelectorNegation;
import io.github.lucaseasedup.logit.util.BlockUtils;
import io.github.lucaseasedup.logit.util.CollectionUtils;
import io.github.lucaseasedup.logit.util.JoinMessageGenerator;
import io.github.lucaseasedup.logit.util.PlayerUtils;
import io.github.lucaseasedup.logit.util.QuitMessageGenerator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class PlayerEventListener extends LogItCoreObject implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL)
    private void onLogin(final PlayerLoginEvent event)
    {
        if (!Result.ALLOWED.equals(event.getResult()))
            return;
        
        Player player = event.getPlayer();
        
        onLogin(player, new PlayerKicker()
        {
            @Override
            public void kick(Player player, String message)
            {
                event.disallow(Result.KICK_OTHER, message);
            }
        });
    }
    
    public void onLogin(Player player, PlayerKicker kicker)
    {
        if (player == null || kicker == null)
            throw new IllegalArgumentException();
        
        String username = player.getName().toLowerCase();
        
        Account account = getAccountManager().selectAccount(username, Arrays.asList(
                keys().username(),
                keys().login_session(),
                keys().is_locked(),
                keys().display_name(),
                keys().persistence()
        ));
        
        if (account != null)
        {
            String displayName = account.getDisplayName();
            
            if (!StringUtils.isBlank(displayName) && !player.getName().equals(displayName)
                    && getConfig("config.yml").getBoolean("usernameCaseMismatch.kick"))
            {
                kicker.kick(player, t("usernameCaseMismatch.kick")
                        .replace("{0}", displayName));
                
                return;
            }
            
            if (account.isLocked())
            {
                kicker.kick(player, t("acclock.success.self"));
                
                return;
            }
        }
        
        int minUsernameLength = getConfig("secret.yml").getInt("username.minLength");
        int maxUsernameLength = getConfig("secret.yml").getInt("username.maxLength");
        
        if (StringUtils.isBlank(username))
        {
            kicker.kick(player, t("usernameBlank"));
        }
        else if (username.length() < minUsernameLength)
        {
            kicker.kick(player, t("usernameTooShort")
                    .replace("{0}", String.valueOf(minUsernameLength)));
        }
        else if (username.length() > maxUsernameLength)
        {
            kicker.kick(player, t("usernameTooLong")
                    .replace("{0}", String.valueOf(maxUsernameLength)));
        }
        else if (!player.getName().matches(getConfig("secret.yml").getString("username.regex")))
        {
            kicker.kick(player, t("usernameInvalid"));
        }
        else if (CollectionUtils.containsIgnoreCase(username,
                getConfig("config.yml").getStringList("prohibitedUsernames")))
        {
            kicker.kick(player, t("usernameProhibited"));
        }
        else if (PlayerUtils.isAnotherPlayerOnline(player))
        {
            kicker.kick(player, t("usernameAlreadyUsed"));
        }
        else if (getConfig("config.yml").getBoolean("kickUnregistered") && account == null)
        {
            kicker.kick(player, t("kickUnregistered"));
        }
        else
        {
            int freeSlots = Bukkit.getMaxPlayers() - Bukkit.getOnlinePlayers().length;
            List<String> reserveForPlayers =
                    getConfig("config.yml").getStringList("reserveSlots.forPlayers");
            int reservedSlots = 0;
            
            // Calculate how many players for which slots should be reserved are online.
            for (Player p : Bukkit.getOnlinePlayers())
            {
                if (CollectionUtils.containsIgnoreCase(p.getName(), reserveForPlayers))
                {
                    reservedSlots++;
                }
            }
            
            int maxReservedSlots = getConfig("config.yml").getInt("reserveSlots.amount");
            int unusedReservedSlots = maxReservedSlots - reservedSlots;
            int actualFreeSlots = freeSlots - unusedReservedSlots;
            
            if (actualFreeSlots <= 0
                    && !CollectionUtils.containsIgnoreCase(username, reserveForPlayers))
            {
                kicker.kick(player, t("noSlotsFree"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    private void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        
        onJoin(player, new JoinMessage()
        {
            @Override
            public void set(String joinMessage)
            {
                event.setJoinMessage(joinMessage);
            }
        });
    }
    
    public void onJoin(final Player player, JoinMessage joinMessage)
    {
        if (player == null || joinMessage == null)
            throw new IllegalArgumentException();
        
        String username = player.getName().toLowerCase();
        String ip = getPlayerIp(player);
        UUID uuid = player.getUniqueId();
        
        joinMessage.set(null);
        
        if (getSessionManager().getSession(player) == null)
        {
            getSessionManager().createSession(player);
        }
        
        long validnessTime = getConfig("config.yml")
                .getTime("loginSessions.validnessTime", TimeUnit.SECONDS);
        
        List<Account> uuidMatchedAccounts = getAccountManager().selectAccounts(
                keys().getNames(),
                new SelectorBinary(
                        new SelectorCondition(keys().uuid(), Infix.EQUALS, uuid.toString()),
                        Infix.AND,
                        new SelectorNegation(
                                new SelectorCondition(keys().username(), Infix.CONTAINS, "$")
                        )
                )
        );
        
        Account account = getAccountManager().selectAccount(username, Arrays.asList(
                keys().username(),
                keys().uuid(),
                keys().login_session(),
                keys().display_name(),
                keys().persistence()
        ));
        
        if (uuidMatchedAccounts != null && !uuidMatchedAccounts.isEmpty())
        {
            Account uuidMatchedAccount = uuidMatchedAccounts.get(0);
            String uuidMatchedUsername = uuidMatchedAccount.getUsername();
            
            if (!uuidMatchedUsername.equalsIgnoreCase(username))
            {
                if (account != null)
                {
                    getAccountManager().renameAccount(username,
                            username + "$" + account.getUuid());
                }
                
                getAccountManager().renameAccount(uuidMatchedUsername, username);
            }
        }
        
        if (account != null)
        {
            if (StringUtils.isBlank(account.getUuid()))
            {
                account.setUuid(uuid);
            }
            
            if (getConfig("config.yml").getBoolean("loginSessions.enabled") && validnessTime > 0)
            {
                String loginSession = account.getLoginSession();
                
                if (!StringUtils.isBlank(loginSession))
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
                            getSessionManager().startSession(player);
                        }
                    }
                }
            }
            
            String displayName = account.getDisplayName();
            
            if (!StringUtils.isBlank(displayName) && !player.getName().equals(displayName)
                    && getConfig("config.yml").getBoolean("usernameCaseMismatch.warning"))
            {
                getMessageDispatcher().dispatchMessage(username, t("usernameCaseMismatch.warning")
                        .replace("{0}", displayName), 4L);
            }
            
            boolean isPremium = BukkitSmerfHook.isPremium(player);
            boolean premiumTakeoverEnabled = getConfig("config.yml")
                    .getBoolean("premiumTakeover.enabled");
            String promptOn = getConfig("config.yml")
                    .getString("premiumTakeover.promptOn");
            
            if (isPremium && premiumTakeoverEnabled && promptOn.equals("join"))
            {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        if (!getSessionManager().isSessionAlive(player))
                        {
                            sendMsg(player, t("takeover.prompt"));
                        }
                    }
                }.runTaskLater(getPlugin(), 140L);
            }
        }
        else
        {
            if (getConfig("config.yml").getBoolean("waitingRoom.enabled"))
            {
                player.teleport(getCore().getWaitingRoomLocation());
            }
        }
        
        if (getSessionManager().isSessionAlive(player) || !getCore().isPlayerForcedToLogIn(player))
        {
            if (!getConfig("config.yml").getBoolean("messages.join.hide")
                    && !VanishNoPacketHook.isVanished(player))
            {
                joinMessage.set(JoinMessageGenerator.generate(player,
                        getConfig("config.yml").getBoolean("messages.join.showWorld")));
            }
        }
        else
        {
            if (account != null)
            {
                getCore().getPersistenceManager().serialize(account, player);
            }
            
            if (!getConfig("config.yml").getBoolean("waitingRoom.enabled"))
            {
                Location playerLocation = player.getLocation();
                Block nearestBlockBelow = BlockUtils.getNearestBlockBelow(playerLocation);
                
                if (nearestBlockBelow.getType().equals(Material.PORTAL))
                {
                    playerLocation = BlockUtils.getNearestSafeSpace(playerLocation, 1000);
                    
                    if (playerLocation != null)
                    {
                        player.teleport(playerLocation);
                    }
                }
            }
            
            long promptPeriod = getConfig("config.yml")
                    .getTime("forceLogin.periodicalPrompt.period", TimeUnit.TICKS);
            
            if (getConfig("config.yml").getBoolean("forceLogin.promptOn.join"))
            {
                if (getConfig("config.yml").getBoolean("forceLogin.periodicalPrompt.enabled"))
                {
                    getMessageDispatcher().dispatchRepeatingForceLoginPrompter(username,
                            TimeUnit.SECONDS.convert(1, TimeUnit.TICKS), promptPeriod);
                }
                else
                {
                    getMessageDispatcher().dispatchForceLoginPrompter(username, 5L);
                }
            }
            else if (getConfig("config.yml").getBoolean("forceLogin.periodicalPrompt.enabled"))
            {
                getMessageDispatcher().dispatchRepeatingForceLoginPrompter(username,
                        TimeUnit.SECONDS.convert(1, TimeUnit.TICKS) + promptPeriod, promptPeriod);
            }
        }
        
        if (player.isDead())
        {
            playersDeadOnJoin.add(player);
        }
        
        if (getConfig("config.yml").getBoolean("groups.enabled"))
        {
            getCore().updatePlayerGroup(player);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    private void onQuit(final PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        
        onQuit(player, new QuitMessage()
        {
            @Override
            public void set(String quitMessage)
            {
                event.setQuitMessage(quitMessage);
            }
        });
    }
    
    public void onQuit(Player player, QuitMessage quitMessage)
    {
        if (player == null || quitMessage == null)
            throw new IllegalArgumentException();
        
        playersDeadOnJoin.remove(player);
        
        Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                keys().username(),
                keys().persistence()
        ));
        
        if (account != null)
        {
            getCore().getPersistenceManager().unserialize(account, player);
        }
        
        if (!getConfig("config.yml").getBoolean("messages.quit.hide")
                && (!getCore().isPlayerForcedToLogIn(player)
                        || getSessionManager().isSessionAlive(player))
                && !VanishNoPacketHook.isVanished(player))
        {
            quitMessage.set(QuitMessageGenerator.generate(player));
        }
        else
        {
            quitMessage.set(null);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig("config.yml").getBoolean("forceLogin.prevent.move")
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
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig("config.yml").getBoolean("forceLogin.prevent.toggleSneak")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onChat_NORMAL(AsyncPlayerChatEvent event)
    {
        if (!isCoreStarted())
            return;
        
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig("config.yml").getBoolean("forceLogin.prevent.chat")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getMessageDispatcher().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChat_MONITOR(AsyncPlayerChatEvent event)
    {
        if (!isCoreStarted())
            return;
        
        if (!getConfig("config.yml").getBoolean("forceLogin.hideChatMessages"))
            return;
        
        Iterator<Player> recipients = event.getRecipients().iterator();
        
        while (recipients.hasNext())
        {
            Player recipient = recipients.next();
            
            if (!getSessionManager().isSessionAlive(recipient)
                    && getCore().isPlayerForcedToLogIn(recipient))
            {
                recipients.remove();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (!isCoreStarted())
            return;
        
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.commandPreprocess"))
            return;
        
        String message = event.getMessage();
        PluginCommand loginCommand = getPlugin().getCommand("login");
        PluginCommand registerCommand = getPlugin().getCommand("register");
        
        if (matchesCommand(loginCommand, message)
                || matchesCommand(registerCommand, message))
        {
            return;
        }
        
        List<String> allowedCommands = getConfig("config.yml")
                .getStringList("forceLogin.allowedCommands");
        
        if (matchesCommand(allowedCommands, message))
        {
            return;
        }
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            getMessageDispatcher().sendForceLoginMessage(player);
        }
    }
    
    private boolean matchesCommand(PluginCommand command, String message)
    {
        if (command == null || message == null)
            throw new IllegalArgumentException();
        
        String commandLabel = command.getLabel();
        
        if (message.equals("/" + commandLabel))
            return true;
        
        if (message.toLowerCase().startsWith("/" + commandLabel.toLowerCase() + " "))
            return true;
        
        return matchesCommand(command.getAliases(), message);
    }
    
    private boolean matchesCommand(Collection<String> matchers, String message)
    {
        if (matchers == null || message == null)
            throw new IllegalArgumentException();
        
        for (String matcher : matchers)
        {
            if (message.equalsIgnoreCase("/" + matcher)
                    || message.toLowerCase().startsWith("/" + matcher.toLowerCase() + " "))
            {
                return true;
            }
        }
        
        return false;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig("config.yml").getBoolean("forceLogin.prevent.interact")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            Block clickedBlock = event.getClickedBlock();
            
            // Check if not on a pressure plate to prevent spamming.
            if (clickedBlock == null || (clickedBlock.getType() != Material.WOOD_PLATE
                    && clickedBlock.getType() != Material.STONE_PLATE))
            {
                if (getConfig("config.yml").getBoolean("forceLogin.promptOn.interact"))
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
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig("config.yml").getBoolean("forceLogin.prevent.interactEntity")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig("config.yml").getBoolean("forceLogin.promptOn.interactEntity"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPickupItem(PlayerPickupItemEvent event)
    {
        if (!getConfig("config.yml").getBoolean("forceLogin.prevent.pickupItem"))
            return;
        
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        Session session = getSessionManager().getSession(player);
        
        if (session != null)
        {
            session.resetInactivityTime();
        }
        
        if (getConfig("config.yml").getBoolean("forceLogin.prevent.dropItem")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            event.setCancelled(true);
            
            if (getConfig("config.yml").getBoolean("forceLogin.promptOn.dropItem"))
            {
                getMessageDispatcher().sendForceLoginMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onChangedWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        
        if (!getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            getMessageDispatcher().sendForceLoginMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    private void onRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();
        
        if (getConfig("config.yml").getBoolean("waitingRoom.enabled")
                && !getSessionManager().isSessionAlive(player)
                && getCore().isPlayerForcedToLogIn(player))
        {
            if (playersDeadOnJoin.contains(player))
            {
                Location respawnLocation = event.getRespawnLocation();
                
                Account account = getAccountManager().selectAccount(player.getName(), Arrays.asList(
                        keys().username(),
                        keys().persistence()
                ));
                
                if (account != null)
                {
                    getPersistenceManager().unserializeUsing(account, player,
                            LocationSerializer.class);
                    getPersistenceManager().serializeUsing(account, player,
                            new LocationSerializer.PlayerlessLocationSerializer(respawnLocation));
                }
            }
            
            event.setRespawnLocation(getCore().getWaitingRoomLocation());
        }
        
        if (playersDeadOnJoin.contains(player))
        {
            playersDeadOnJoin.remove(player);
        }
    }
    
    private final Set<Player> playersDeadOnJoin = new HashSet<>();
}
