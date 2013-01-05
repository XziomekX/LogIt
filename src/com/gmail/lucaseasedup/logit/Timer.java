/*
 * Timer.java
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
package com.gmail.lucaseasedup.logit;

/**
 * @author LucasEasedUp
 */
public class Timer implements Runnable
{
    public Timer(long interval)
    {
        this.interval = interval;
    }
    
    @Override
    public void run()
    {
        if (running)
        {
            elapsed += interval;
        }
    }
    
    public long getInterval()
    {
        return interval;
    }
    
    public boolean isRunning()
    {
        return running;
    }
    
    public long getElapsed()
    {
        return elapsed;
    }
    
    public void start()
    {
        if (!running)
        {
            elapsed = 0L;
            running = true;
        }
    }
    
    public void stop()
    {
        running = false;
    }
    
    public void resume()
    {
        running = true;
    }
    
    public void reset()
    {
        elapsed = 0L;
    }
    
    private final long interval;
    
    private boolean running = false;
    private long elapsed = 0L;
}
