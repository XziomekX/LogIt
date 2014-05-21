/*
 * Wizard.java
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
package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.util.MessageHelper.sendMsg;
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
    
    @EventHandler(priority = EventPriority.LOW)
    public final void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getPlayer() == sender)
        {
            onMessage(event.getMessage());
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public final void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.getPlayer() == sender)
        {
            onMessage(event.getMessage());
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public final void onServerCommand(ServerCommandEvent event)
    {
        if (event.getSender() == sender)
        {
            onMessage(event.getCommand());
            event.setCommand("$logit-nop-command");
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public final void onPlayerQuit(PlayerQuitEvent event)
    {
        if (event.getPlayer() == sender)
        {
            cancelWizard();
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
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
    
    private final CommandSender sender;
    private final Object initStep;
    private Object step = null;
}
