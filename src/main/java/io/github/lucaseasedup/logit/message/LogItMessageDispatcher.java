package io.github.lucaseasedup.logit.message;

import static io.github.lucaseasedup.logit.message.MessageHelper.broadcastMsgExcept;
import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.config.TimeUnit;
import io.github.lucaseasedup.logit.hooks.EssentialsHook;
import io.github.lucaseasedup.logit.hooks.VanishNoPacketHook;
import io.github.lucaseasedup.logit.locale.Locale;
import java.util.Arrays;
import java.util.Hashtable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class LogItMessageDispatcher extends LogItCoreObject implements Listener
{
    @Override
    public void dispose()
    {
        if (forceLoginPromptIntervals != null)
        {
            forceLoginPromptIntervals.clear();
            forceLoginPromptIntervals = null;
        }
    }
    
    public void dispatchMessage(final String username, final String message, long delay)
    {
        if (username == null || message == null || delay < 0)
            throw new IllegalArgumentException();
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Player player = Bukkit.getPlayerExact(username);
                
                if (player != null)
                {
                    player.sendMessage(message);
                }
            }
        }.runTaskLater(getPlugin(), delay);
    }
    
    public void dispatchMessage(Player player, String message, long delay)
    {
        dispatchMessage(player.getName(), message, delay);
    }
    
    /**
     * Sends a message to the given player telling them either to log in or to register.
     * 
     * <p> This method's behavior may be altered by the configuration file.
     * 
     * @param player the player to whom the message will be sent.
     * 
     * @throws IllegalArgumentException if {@code player} is {@code null}.
     */
    public void sendForceLoginMessage(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        long minInterval = getConfig("config.yml")
                .getTime("forceLogin.prompt.minInterval", TimeUnit.MILLISECONDS);
        
        if (minInterval > 0)
        {
            long currentTimeMillis = System.currentTimeMillis();
            Long playerInterval = forceLoginPromptIntervals.get(player);
            
            if (playerInterval != null && currentTimeMillis - playerInterval < minInterval)
                return;
            
            forceLoginPromptIntervals.put(player, currentTimeMillis);
        }
        
        if (getAccountManager().isRegistered(player.getName()))
        {
            if (getConfig("config.yml").getBoolean("forceLogin.prompt.login"))
            {
                if (!getConfig("config.yml").getBoolean("passwords.disable"))
                {
                    sendMsg(player, t("pleaseLogIn"));
                }
                else
                {
                    sendMsg(player, t("pleaseLogIn_noPassword"));
                }
            }
        }
        else
        {
            if (getConfig("config.yml").getBoolean("forceLogin.prompt.register"))
            {
                if (!getConfig("config.yml").getBoolean("passwords.disable"))
                {
                    sendMsg(player, t("pleaseRegister"));
                }
                else
                {
                    sendMsg(player, t("pleaseRegister_noPassword"));
                }
            }
        }
    }
    
    public void dispatchForceLoginPrompter(String username, long delay)
    {
        if (username == null || delay < 0)
            throw new IllegalArgumentException();
        
        new ForceLoginPrompter(username).runTaskLater(getPlugin(), delay);
    }
    
    public void dispatchRepeatingForceLoginPrompter(String username, long delay, long period)
    {
        if (username == null || delay < 0 || period <= 0)
            throw new IllegalArgumentException();
        
        new ForceLoginPrompter(username).runTaskTimer(getPlugin(), delay, period);
    }
    
    /**
     * Broadcasts a join message.
     * 
     * @param player the player who joined.
     */
    public void broadcastJoinMessage(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (VanishNoPacketHook.isVanished(player))
            return;
        
        if (EssentialsHook.isVanished(player))
            return;
        
        String joinMessage = JoinMessageGenerator.generate(player,
                getConfig("config.yml").getBoolean("messages.join.showWorld"));
        
        broadcastMsgExcept(joinMessage, Arrays.asList(player.getName()));
    }
    
    /**
     * Broadcasts a quit message.
     * 
     * @param player the player who quit.
     */
    public void broadcastQuitMessage(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException();
        
        if (VanishNoPacketHook.isVanished(player))
            return;
        
        if (EssentialsHook.isVanished(player))
            return;
        
        String quitMessage = QuitMessageGenerator.generate(player);
        
        broadcastMsgExcept(quitMessage, Arrays.asList(player.getName()));
    }
    
    public void sendCooldownMessage(String username, long cooldownMillis)
    {
        if (username == null)
            throw new IllegalArgumentException();
        
        Locale activeLocale = getLocaleManager().getActiveLocale();
        int cooldownSecs = (int) TimeUnit.MILLISECONDS.convert(cooldownMillis, TimeUnit.SECONDS);
        String cooldownText = activeLocale.stringifySeconds(cooldownSecs);
        
        if (cooldownMillis >= 2000L)
        {
            sendMsg(username, t("cooldown.moreThanSecond")
                    .replace("{0}", cooldownText));
        }
        else
        {
            sendMsg(username, t("cooldown.secondOrLess")
                    .replace("{0}", cooldownText));
        }
    }
    
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        forceLoginPromptIntervals.remove(event.getPlayer());
    }
    
    @EventHandler
    private void onPlayerKick(PlayerKickEvent event)
    {
        forceLoginPromptIntervals.remove(event.getPlayer());
    }
    
    private final class ForceLoginPrompter extends BukkitRunnable
    {
        public ForceLoginPrompter(String username)
        {
            this.username = username;
        }
        
        @Override
        public void run()
        {
            Player player = Bukkit.getPlayerExact(username);
            
            if (player == null || !isCoreStarted())
            {
                cancel();
            }
            else if (getCore().isPlayerForcedToLogIn(player))
            {
                if (!getSessionManager().isSessionAlive(player))
                {
                    sendForceLoginMessage(player);
                }
                else
                {
                    cancel();
                }
            }
        }
        
        private final String username;
    }
    
    private Hashtable<Player, Long> forceLoginPromptIntervals = new Hashtable<>();
}
