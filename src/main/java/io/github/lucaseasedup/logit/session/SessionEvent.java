package io.github.lucaseasedup.logit.session;

import io.github.lucaseasedup.logit.common.CancellableEvent;

public abstract class SessionEvent extends CancellableEvent
{
    /* package */ SessionEvent(String username, Session session)
    {
        this.username = username;
        this.session = session;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public Session getSession()
    {
        return session;
    }
    
    private final String username;
    private final Session session;
}
