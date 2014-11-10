package io.github.lucaseasedup.logit.session;

import org.bukkit.Location;

/**
 * Represents a player session. It holds a time-based status,
 * a player IP address and inactivity time of the player.
 */
public final class Session
{
    /**
     * Creates a new {@code Session} object with a new status equal
     * to {@code -1L}.
     * 
     * @param ip an IP address of this session, can be {@code null}.
     */
    public Session(String ip)
    {
        this.ip = ip;
    }
    
    /**
     * Returns an IP address associated with this session.
     * 
     * @return an IP address or {@code null}.
     */
    public String getIp()
    {
        return ip;
    }
    
    /**
     * Sets a new IP address for this session.
     * 
     * @param ip new IP address, can be {@code null}.
     */
    public void setIp(String ip)
    {
        this.ip = ip;
    }
    
    /**
     * Returns session status.
     * 
     * <p> Values above or equal to {@code 0} mean that the session is
     * alive (logged-in state). Values below {@code 0} mean that the session
     * is not alive (logged-out state).
     * 
     * @return the session status.
     */
    public long getStatus()
    {
        return status;
    }
    
    /**
     * Sets a new session status.
     * 
     * @param status new session status.
     */
    public void setStatus(long status)
    {
        this.status = status;
    }
    
    /**
     * Updates status of this session by adding {@code update} to the actual status.
     * 
     * @param delta a value by which the status will be changed.
     */
    public void updateStatus(long delta)
    {
        status += delta;
    }
    
    /**
     * Checks whether this session is alive.
     * 
     * <p> A session is alive when its status is greater or equal to {@code 0}.
     * 
     * @return {@code true} if the session is alive; {@code false} otherwise.
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
     * @param delta a value in server ticks by which the inactivity time will be changed.
     */
    /* package */ void advanceInactivityTime(long delta)
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
    
    public Location getLastForceLoginLocation()
    {
        return lastForceLoginLocation;
    }
    
    public void setLastForceLoginLocation(Location lastForceLoginLocation)
    {
        this.lastForceLoginLocation = lastForceLoginLocation;
    }
    
    private String ip;
    private long status = -1L;
    private long inactivityTime = 0L;
    private Location lastForceLoginLocation = null;
}
