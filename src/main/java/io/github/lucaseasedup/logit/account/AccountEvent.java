/*
 * AccountEvent.java
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
package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.CancellableEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.HandlerList;

public abstract class AccountEvent extends CancellableEvent
{
    public AccountEvent()
    {
        this.successTasks = new ArrayList<>();
        this.failureTasks = new ArrayList<>();
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
    
    /**
     * Schedules a task to be executed when the action following
     * this event is succeeds.
     * 
     * @param task the task to be scheduled.
     * 
     * @throws IllegalArgumentException if {@code task} is {@code null}.
     * @throws IllegalStateException    if tasks has already been executed.
     */
    public final void scheduleSuccessTask(Runnable task)
    {
        if (task == null)
            throw new IllegalArgumentException();
        
        if (successTasks == null)
            throw new IllegalStateException();
        
        successTasks.add(task);
    }
    
    /**
     * Schedules a task to be executed when the action following
     * this event is fails.
     * 
     * @param task the task to be scheduled.
     * 
     * @throws IllegalArgumentException if {@code task} is {@code null}.
     * @throws IllegalStateException    if tasks has already been executed.
     */
    public final void scheduleFailureTask(Runnable task)
    {
        if (task == null)
            throw new IllegalArgumentException();
        
        if (failureTasks == null)
            throw new IllegalStateException();
        
        failureTasks.add(task);
    }
    
    /* package */ final void executeSuccessTasks()
    {
        if (successTasks == null)
        {
            invalidateTaskLists();
            
            throw new IllegalStateException();
        }
        
        for (Runnable task : successTasks)
        {
            task.run();
        }
        
        invalidateTaskLists();
    }
    
    /* package */ final void executeFailureTasks()
    {
        if (failureTasks == null)
        {
            invalidateTaskLists();
            
            throw new IllegalStateException();
        }
        
        for (Runnable task : failureTasks)
        {
            task.run();
        }
        
        invalidateTaskLists();
    }
    
    private void invalidateTaskLists()
    {
        successTasks = null;
        failureTasks = null;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    private static final HandlerList handlers = new HandlerList();
    
    private List<Runnable> successTasks;
    private List<Runnable> failureTasks;
}
