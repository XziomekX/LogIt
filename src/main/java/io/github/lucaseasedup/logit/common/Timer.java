package io.github.lucaseasedup.logit.common;

public final class Timer implements Runnable
{
    /**
     * Constructs a new {@code Timer}.
     * 
     * @param interval the timer interval (if {@code interval == 1}
     *                 each {@link #run()} call will advance the timer by exactly 1 unit).
     * 
     * @throws IllegalArgumentException if {@code interval} is less than zero.
     */
    public Timer(long interval)
    {
        if (interval < 0)
            throw new IllegalArgumentException();
        
        this.interval = interval;
    }
    
    @Override
    public void run()
    {
        advance();
    }
    
    /**
     * Advances this timer by an interval.
     */
    public void advance()
    {
        if (running)
        {
            elapsed += interval;
        }
    }
    
    public void start()
    {
        if (!running)
        {
            elapsed = 0L;
            running = true;
        }
    }
    
    public void resume()
    {
        running = true;
    }
    
    public void stop()
    {
        running = false;
    }
    
    public void reset()
    {
        elapsed = 0L;
    }
    
    public long getElapsed()
    {
        return elapsed;
    }
    
    public boolean isRunning()
    {
        return running;
    }
    
    public long getInterval()
    {
        return interval;
    }
    
    private final long interval;
    private boolean running = false;
    private long elapsed = 0L;
}
