/*
 * AccountEvent.java
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
package io.github.lucaseasedup.logit.account;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author LucasEasedUp
 */
public abstract class AccountEvent extends Event implements Cancellable
{
    public AccountEvent(Account account)
    {
        this.account = account;
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
    
    @Override
    public final boolean isCancelled()
    {
        return cancelled;
    }
    
    @Override
    public final void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
    
    public Account getAccount()
    {
        return account;
    }
    
    /**
     * Equal to <code>getAccount().get("logit.accounts.username")</code>.
     * 
     * @return Username.
     */
    public String getUsername()
    {
        return account.getString("logit.accounts.username");
    }
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Account account;
    private boolean cancelled = false;
}
