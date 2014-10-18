package io.github.lucaseasedup.logit.session;

public final class SessionCreateEvent extends SessionEvent
{
    /* package */ SessionCreateEvent(String username)
    {
        super(username, null);
    }
    
    /**
     * Always returns <code>null</code>
     */
    @Override
    public Session getSession()
    {
        return null;
    }
}
