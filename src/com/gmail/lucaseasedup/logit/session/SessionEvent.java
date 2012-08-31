/*
 * SessionEvent.java
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
package com.gmail.lucaseasedup.logit.session;

import com.gmail.lucaseasedup.logit.session.Session;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author LucasEasedUp
 */
public abstract class SessionEvent extends Event
{
    public SessionEvent(String username, Session session)
    {
        this.username = username.toLowerCase();
        this.session  = session;
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public String getUsername()
    {
        return username;
    }
    
    public Session getSession()
    {
        return session;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    private final String username;
    private final Session session;
}
