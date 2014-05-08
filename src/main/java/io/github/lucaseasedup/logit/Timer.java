/*
 * Timer.java
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
package io.github.lucaseasedup.logit;

public class Timer implements Runnable
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
