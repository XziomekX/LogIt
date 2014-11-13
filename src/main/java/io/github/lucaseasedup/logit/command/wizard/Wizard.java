package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.message.MessageHelper.sendMsg;
import static io.github.lucaseasedup.logit.message.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCoreObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

public abstract class Wizard extends LogItCoreObject implements Listener
{
    public Wizard(CommandSender sender, Object initStep)
    {
        this.sender = sender;
        this.initStep = initStep;
    }
    
    public final void createWizard()
    {
        step = initStep;
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        onCreate();
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public final void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getPlayer() == sender)
        {
            if (event.getMessage().equals(MAGIC_QUIT_WORD))
            {
                sendMessage(t("wizardCancelled"));
                cancelWizard();
            }
            else
            {
                onMessage(event.getMessage());
            }
            
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public final void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.getPlayer() == sender)
        {
            sendMessage(t("wizardCancelled"));
            cancelWizard();
            Bukkit.dispatchCommand(
                    event.getPlayer(), event.getMessage().substring(1)
            );
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public final void onServerCommand(ServerCommandEvent event)
    {
        if (event.getSender() == sender)
        {
            if (event.getCommand().equals(MAGIC_QUIT_WORD))
            {
                sendMessage(t("wizardCancelled"));
                cancelWizard();
            }
            else
            {
                onMessage(event.getCommand());
            }
            
            event.setCommand("$logit-nop-command");
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public final void onPlayerQuit(PlayerQuitEvent event)
    {
        if (event.getPlayer() == sender)
        {
            cancelWizard();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public final void onPlayerKick(PlayerKickEvent event)
    {
        if (event.getPlayer() == sender)
        {
            cancelWizard();
        }
    }
    
    public final Object getCurrentStep()
    {
        return step;
    }
    
    protected abstract void onCreate();
    protected abstract void onMessage(String message);
    
    protected final void sendMessage(String message)
    {
        sendMsg(getSender(), message);
    }
    
    protected final void cancelWizard()
    {
        HandlerList.unregisterAll(this);
    }
    
    protected final CommandSender getSender()
    {
        return sender;
    }
    
    protected final void updateStep(Object step)
    {
        this.step = step;
    }
    
    private static final String MAGIC_QUIT_WORD = ":E1,X2/$";
    
    private final CommandSender sender;
    private final Object initStep;
    private Object step = null;
}
