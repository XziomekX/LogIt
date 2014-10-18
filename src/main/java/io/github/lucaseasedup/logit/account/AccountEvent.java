package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.common.CancellableEvent;
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
     * this event succeeds.
     * 
     * @param task the task to be scheduled.
     * 
     * @throws IllegalArgumentException if {@code task} is {@code null}.
     * @throws IllegalStateException    if tasks have already been executed.
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
     * this event fails.
     * 
     * @param task the task to be scheduled.
     * 
     * @throws IllegalArgumentException if {@code task} is {@code null}.
     * @throws IllegalStateException    if tasks have already been executed.
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
