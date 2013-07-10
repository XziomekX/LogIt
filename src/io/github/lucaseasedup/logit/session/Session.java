/*
 * Session.java
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
package io.github.lucaseasedup.logit.session;

/**
 * @author LucasEasedUp
 */
public class Session
{
    public Session(String ip)
    {
        this.ip = ip;
    }
    
    /**
     * Returns an IP address associated with the session.
     * 
     * @return IP address.
     */
    public String getIp()
    {
        return ip;
    }
    
    /**
     * Returns session status.
     * <p/>
     * Values above or equal to 0 mean "session alive" (logged in). Values below 0 mean "session ended" (logged out).
     * 
     * @return Session status.
     */
    public long getStatus()
    {
        return status;
    }
    
    /**
     * Sets session status to value of {@code status}.
     * 
     * @param status Session status.
     */
    public void setStatus(long status)
    {
        this.status = status;
    }
    
    /**
     * Updates session status by adding {@code update} to current status.
     * 
     * @param update Update value.
     */
    public void updateStatus(long update)
    {
        status += update;
    }
    
    /**
     * Check if the session is alive.
     * <p/>
     * A session is alive when its status is below or equal to 0.
     * 
     * @return True if the session is alive.
     */
    public boolean isAlive()
    {
        return status >= 0L;
    }
    
    private String ip;
    private long status = -1L;
}
