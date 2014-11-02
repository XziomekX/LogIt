package io.github.lucaseasedup.logit.account;

import io.github.lucaseasedup.logit.common.CancellableEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class AccountEvent extends CancellableEvent
{
    /* package */ AccountEvent()
    {
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
    
    private List<Runnable> successTasks = new ArrayList<>();
    private List<Runnable> failureTasks = new ArrayList<>();
}
