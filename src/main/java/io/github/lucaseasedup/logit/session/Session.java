/*
 * Session.java
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
package io.github.lucaseasedup.logit.session;

public final class Session
{
    public Session(String ip)
    {
        if (ip == null)
            throw new IllegalArgumentException();
        
        this.ip = ip;
    }
    
    /**
     * Returns an IP address associated with this session.
     * 
     * @return IP address.
     */
    public String getIp()
    {
        return ip;
    }
    
    public void setIp(String ip)
    {
        if (ip == null)
            throw new IllegalArgumentException();
        
        this.ip = ip;
    }
    
    /**
     * Returns session status.
     * 
     * <p> Values above or equal to {@code 0} mean <i>session alive/logged in</i>.
     * Values below {@code 0} mean <i>session ended/logged out</i>.
     * 
     * @return session status.
     */
    public long getStatus()
    {
        return status;
    }
    
    /**
     * Sets session status to value of {@code status}.
     * 
     * @param status the new session status.
     */
    public void setStatus(long status)
    {
        this.status = status;
    }
    
    /**
     * Updates session status by adding {@code update} to the current status.
     * 
     * @param update update value.
     */
    public void updateStatus(long update)
    {
        status += update;
    }
    
    /**
     * Checks if the session is alive.
     * 
     * <p> A session is alive when its status is below or equal to {@code 0}.
     * 
     * @return {@code true} if the session is alive.
     */
    public boolean isAlive()
    {
        return status >= 0L;
    }
    
    /**
     * Returns player's inactivity time in server ticks.
     * 
     * @return player's inactivity time in server ticks.
     */
    public long getInactivityTime()
    {
        return inactivityTime;
    }
    
    /**
     * Adds a delta to player's inactivity time.
     * 
     * @param delta the delta time in server ticks.
     */
    public void updateInactivityTime(long delta)
    {
        inactivityTime += delta;
    }
    
    /**
     * Resets player's inactivity time so that it's equal to {@code 0L}.
     */
    public void resetInactivityTime()
    {
        inactivityTime = 0L;
    }
    
    private String ip;
    private long status = -1L;
    private long inactivityTime = 0L;
}
