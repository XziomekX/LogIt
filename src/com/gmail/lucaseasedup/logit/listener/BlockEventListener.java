/*
 * BlockEventListener.java
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
package com.gmail.lucaseasedup.logit.listener;

import com.gmail.lucaseasedup.logit.LogItCore;
import static com.gmail.lucaseasedup.logit.util.MessageSender.sendForceLoginMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author LucasEasedUp
 */
public class BlockEventListener extends EventListener
{
    public BlockEventListener(LogItCore core)
    {
        super(core);
    }
    
    @EventHandler
    private void onPlace(BlockPlaceEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.block-place"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            sendForceLoginMessage(player, core.getAccountManager());
        }
    }
    
    @EventHandler
    private void onBreak(BlockBreakEvent event)
    {
        if (!core.getConfig().getBoolean("force-login.prevent.block-break"))
            return;
        
        Player player = event.getPlayer();
        
        if (!core.getSessionManager().isSessionAlive(player) && core.isPlayerForcedToLogin(player))
        {
            event.setCancelled(true);
            sendForceLoginMessage(player, core.getAccountManager());
        }
    }
}
